package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyAssignment;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyResolutionEntity;
import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyAssignmentSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository;
import com.ifortex.internship.emergencyservice.repository.UserAllergyRepository;
import com.ifortex.internship.emergencyservice.repository.UserDiseaseRepository;
import com.ifortex.internship.emergencyservice.util.EmergencyAssignmentMapper;
import com.ifortex.internship.emergencyservice.util.EmergencyLocationMapper;
import com.ifortex.internship.emergencyservice.util.EmergencySnapshotMapper;
import com.ifortex.internship.emergencyservice.util.UserAllergyMapper;
import com.ifortex.internship.emergencyservice.util.UserDiseaseMapper;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmergencySnapshotService {

    public static final String EXCEPTION_NO_ONGOING_EMERGENCY_FOUND = "No ongoing emergency found";
    public static final String LOG_UPDATED_SNAPSHOT_SAVED_FOR_EMERGENCY = "Updated snapshot saved for emergency {}";
    public static final String LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_CLIENT = "No ongoing emergency found for client: {}";

    SymptomService symptomService;
    UserAllergyMapper userAllergyMapper;
    UserDiseaseMapper userDiseaseMapper;
    EmergencyRepository emergencyRepository;
    AuthenticationFacade authenticationFacade;
    UserAllergyRepository userAllergyRepository;
    UserDiseaseRepository userDiseaseRepository;
    EmergencyLocationMapper emergencyLocationMapper;
    EmergencySnapshotMapper emergencySnapshotMapper;
    EmergencyAssignmentMapper emergencyAssignmentMapper;
    EmergencySnapshotRepository emergencySnapshotRepository;

    @Transactional
    public void createSnapshot(Emergency emergency, List<UUID> symptomsIds) {
        UUID clientId = emergency.getClientId();
        String firstName = authenticationFacade.getUserFirstNameFromAuthentication();
        Objects.requireNonNull(firstName, "First name is missing in security context");

        log.info("Creating snapshot for emergency [{}] for client [{}]", emergency.getId(), clientId);

        List<UserAllergyDto> userAllergies = userAllergyMapper.toDtoList(userAllergyRepository.findByUserId(clientId));
        List<UserDiseaseDto> userDiseases = userDiseaseMapper.toDtoList(userDiseaseRepository.findByUserId(clientId));

        List<SymptomDto> symptoms = symptomService.collectParentsSymptomsForEmergency(emergency.getId().toString(), symptomsIds);

        EmergencySnapshot emergencySnapshot = new EmergencySnapshot()
            .setId(emergency.getId().toString())
            .setCreatedAt(emergency.getCreatedAt())
            .setStatus(emergency.getStatus())
            .setClientFirstName(firstName)
            .setLatitude(emergency.getLatitude())
            .setLongitude(emergency.getLongitude())
            .setParamedicLocations(new ArrayList<>())
            .setSymptoms(symptoms)
            .setAllergies(userAllergies)
            .setDiseases(userDiseases);
        emergencySnapshotRepository.save(emergencySnapshot);
        log.debug("Snapshot for emergency [{}] saved successfully", emergency.getId());
    }

    @Transactional
    public void addSymptomsForCurrentEmergency(UpdateEmergencySymptomsRequest request, UUID clientId) {
        log.debug("Adding symptoms for client: {}", clientId);

        EmergencySnapshot emergencySnapshot = getEmergencySnapshotByClientIdAndStatus(clientId, EmergencyStatus.ONGOING);

        List<SymptomDto> newSymptoms = symptomService.collectParentsSymptomsForEmergency(emergencySnapshot.getId(), request.symptoms());
        log.debug("Collected {} symptom(s) for emergency {}", newSymptoms.size(), emergencySnapshot.getId());

        List<SymptomDto> currentSymptoms = emergencySnapshot.getSymptoms();
        if (currentSymptoms == null) {
            currentSymptoms = new ArrayList<>();
            emergencySnapshot.setSymptoms(currentSymptoms);
            log.debug("Initialized symptoms list in snapshot for emergency {}", emergencySnapshot.getId());
        }

        Set<UUID> existingSymptomIds = currentSymptoms.stream()
            .map(SymptomDto::id)
            .collect(Collectors.toSet());

        int addedCount = 0;
        for (SymptomDto symptom : newSymptoms) {
            if (!existingSymptomIds.contains(symptom.id())) {
                currentSymptoms.add(symptom);
                addedCount++;
            }
        }
        log.debug("Added {} new symptom(s) to snapshot for emergency {}", addedCount, emergencySnapshot.getId());

        emergencySnapshotRepository.save(emergencySnapshot);
        log.info(LOG_UPDATED_SNAPSHOT_SAVED_FOR_EMERGENCY, emergencySnapshot.getId());
    }

    @Transactional
    public void deleteSymptomsForCurrentEmergency(UpdateEmergencySymptomsRequest request, UUID clientId) {
        log.debug("Deleting symptoms for client: {}", clientId);

        EmergencySnapshot emergencySnapshot = getEmergencySnapshotByClientIdAndStatus(clientId, EmergencyStatus.ONGOING);

        List<SymptomDto> symptomsToRemove = symptomService.collectChildSymptomsForEmergency(emergencySnapshot.getId(), request.symptoms());
        log.debug("Collected {} symptom(s) for removal for emergency {}", symptomsToRemove.size(), emergencySnapshot.getId());

        List<SymptomDto> currentSymptoms = emergencySnapshot.getSymptoms();
        if (currentSymptoms == null || currentSymptoms.isEmpty()) {
            log.info("No symptoms present in snapshot for emergency {}. Nothing to remove.", emergencySnapshot.getId());
            return;
        }

        Set<UUID> removeIds = symptomsToRemove.stream()
            .map(SymptomDto::id)
            .collect(Collectors.toSet());

        int initialSize = currentSymptoms.size();
        currentSymptoms.removeIf(symptom -> removeIds.contains(symptom.id()));
        int removedCount = initialSize - currentSymptoms.size();
        log.debug("Removed {} symptom(s) from snapshot for emergency {}", removedCount, emergencySnapshot.getId());

        emergencySnapshotRepository.save(emergencySnapshot);
        log.info(LOG_UPDATED_SNAPSHOT_SAVED_FOR_EMERGENCY, emergencySnapshot.getId());
    }

    public List<EmergencySymptomListDto> getSymptomsForCurrentEmergency(UUID clientId) {
        log.debug("Fetching current emergency symptoms for client: {}", clientId);

        EmergencySnapshot snapshot = getEmergencySnapshotByClientIdAndStatus(clientId, EmergencyStatus.ONGOING);

        List<SymptomDto> symptoms = snapshot.getSymptoms();
        if (symptoms == null || symptoms.isEmpty()) {
            log.info("No symptoms found in the current emergency for client: {}", clientId);
            return Collections.emptyList();
        }
        var symptomTree = emergencySnapshotMapper.buildSymptomTree(symptoms);
        log.debug("Fetched all symptoms for client: {}", clientId);
        return symptomTree;
    }

    public EmergencySnapshot getEmergencySnapshotByClientIdAndStatus(UUID clientId, EmergencyStatus status) {
        var emergency = getEmergencyByClientIdAndStatus(clientId, status);
        return emergencySnapshotRepository.findById(emergency.getId().toString())
            .orElseThrow(() -> {
                log.error(LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_CLIENT, clientId);
                return new EntityNotFoundException(EXCEPTION_NO_ONGOING_EMERGENCY_FOUND);
            });
    }

    @Transactional
    public Optional<ParamedicEmergencyViewDto> getAssignedEmergency(UUID paramedicId) {
        log.debug("Fetching assigned emergency for paramedic {}", paramedicId);

        var emergency = emergencyRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)
            .orElseThrow(
                () -> {
                    log.error("No ongoing emergency found for paramedic: {}", paramedicId);
                    return new EntityNotFoundException(EXCEPTION_NO_ONGOING_EMERGENCY_FOUND);
                }
            );

        Optional<EmergencySnapshot> emergencySnapshotOpt = emergencySnapshotRepository.findById(emergency.getId().toString());
        if (emergencySnapshotOpt.isEmpty()) {
            log.info("No ongoing emergency assigned to paramedic {}", paramedicId);
        }
        return emergencySnapshotOpt.map(emergencySnapshotMapper::toParamedicViewDto);
    }

    @Transactional
    public void updateSnapshotAfterCancellationByParamedic(Emergency emergency,
                                                           EmergencyAssignment assignment,
                                                           BigDecimal latitude,
                                                           BigDecimal longitude) {
        log.info("Updating snapshot with canceled assignment for emergency {}", emergency.getId());

        String emergencyId = emergency.getId().toString();
        EmergencySnapshot snapshot = getEmergencySnapshotByEmergencyId(emergencyId);
        snapshot.setParamedicId(null);

        ParamedicEmergencyLocationSnapshot locationSnapshot = new ParamedicEmergencyLocationSnapshot()
            .setEmergencyId(emergency.getId())
            .setParamedicId(assignment.getParamedicId())
            .setLatitude(latitude)
            .setLongitude(longitude)
            .setTimestamp(Instant.now())
            .setLocationType(EmergencyLocationType.CANCELLED);

        List<ParamedicEmergencyLocationSnapshot> locations = snapshot.getParamedicLocations();
        if (locations == null) {
            locations = new ArrayList<>();
            snapshot.setParamedicLocations(locations);
        }
        locations.add(locationSnapshot);
        log.debug("Cancellation Location added to snapshot for emergency: {}", emergencyId);

        EmergencyAssignmentSnapshot assignmentSnapshot = EmergencyAssignmentSnapshot.builder()
            .id(assignment.getId())
            .emergencyId(emergency.getId())
            .paramedicId(assignment.getParamedicId())
            .assignedAt(assignment.getAssignedAt())
            .canceledAt(Instant.now())
            .cancellationReason(assignment.getCancellationReason().getDescription())
            .cancellationComment(assignment.getCancellationComment())
            .build();

        List<EmergencyAssignmentSnapshot> assignmentSnapshots = snapshot.getAssignments();
        if (assignmentSnapshots == null) {
            assignmentSnapshots = new ArrayList<>();
            snapshot.setAssignments(assignmentSnapshots);
        }
        assignmentSnapshots.add(assignmentSnapshot);

        emergencySnapshotRepository.save(snapshot);
        log.info("Snapshot updated with canceled assignment for emergency {}", emergencyId);
    }

    public void updateEmergencySnapshotAfterParamedicAssign(Emergency emergency,
                                                            EmergencyAssignment assignment,
                                                            ParamedicEmergencyLocation paramedicEmergencyLocation) {

        EmergencySnapshot snapshot = getEmergencySnapshotByEmergencyId(emergency.getId().toString());
        log.debug("Updating snapshot after paramedic: {} assigned for emergency {}", emergency.getParamedicId(), emergency.getId());
        snapshot.setParamedicId(emergency.getParamedicId());

        EmergencyAssignmentSnapshot assignmentSnapshot = emergencyAssignmentMapper.toSnapshot(assignment);
        if (snapshot.getAssignments() == null) {
            snapshot.setAssignments(new ArrayList<>());
        }
        snapshot.getAssignments().add(assignmentSnapshot);
        log.debug("Added assignment snapshot for paramedic {} to emergency {}", assignment.getParamedicId(), emergency.getId());

        var snapshotLocation = emergencyLocationMapper.toSnapshot(paramedicEmergencyLocation);
        snapshot.getParamedicLocations().add(snapshotLocation);

        emergencySnapshotRepository.save(snapshot);
        log.debug("Snapshot for emergency {} updated successfully", emergency.getId());
    }

    public void updateSnapshotAfterCompletion(Emergency emergency, ParamedicEmergencyLocation location, EmergencyResolutionEntity resolution) {
        String emergencyId = emergency.getId().toString();
        log.info("Updating snapshot after completion of emergency {}", emergencyId);

        EmergencySnapshot snapshot = getEmergencySnapshotByEmergencyId(emergencyId)
            .setResolution(resolution.getDescription())
            .setResolutionExplanation(emergency.getResolutionExplanation())
            .setStatus(EmergencyStatus.COMPLETED)
            .setClosedAt(Instant.now());

        snapshot.setDuration(Duration.between(snapshot.getCreatedAt(), snapshot.getClosedAt()));

        ParamedicEmergencyLocationSnapshot locationSnapshot = new ParamedicEmergencyLocationSnapshot()
            .setLatitude(location.getLatitude())
            .setLongitude(location.getLongitude())
            .setTimestamp(location.getTimestamp())
            .setParamedicId(emergency.getParamedicId())
            .setLocationType(EmergencyLocationType.FINISHED);

        if (snapshot.getParamedicLocations() == null) {
            snapshot.setParamedicLocations(new ArrayList<>());
        }
        snapshot.getParamedicLocations().add(locationSnapshot);

        emergencySnapshotRepository.save(snapshot);
        log.info("Snapshot updated for completed emergency {}", emergencyId);
    }

    private EmergencySnapshot getEmergencySnapshotByEmergencyId(String emergencyId) {
        return emergencySnapshotRepository.findById(emergencyId)
            .orElseThrow(() -> {
                log.error("Snapshot not found for emergency: {}", emergencyId);
                return new EntityNotFoundException("Snapshot not found for emergency " + emergencyId);
            });
    }

    private Emergency getEmergencyByClientIdAndStatus(UUID clientId, EmergencyStatus status) {
        return emergencyRepository.findByClientIdAndStatus(clientId, status).orElseThrow(
            () -> {
                log.error(LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_CLIENT, clientId);
                return new EntityNotFoundException(EXCEPTION_NO_ONGOING_EMERGENCY_FOUND);
            }
        );
    }
}
