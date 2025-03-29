package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository;
import com.ifortex.internship.emergencyservice.repository.UserAllergyRepository;
import com.ifortex.internship.emergencyservice.repository.UserDiseaseRepository;
import com.ifortex.internship.emergencyservice.util.EmergencyLocationMapper;
import com.ifortex.internship.emergencyservice.util.EmergencySnapshotMapper;
import com.ifortex.internship.emergencyservice.util.UserAllergyMapper;
import com.ifortex.internship.emergencyservice.util.UserDiseaseMapper;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    UserAllergyRepository userAllergyRepository;
    UserDiseaseRepository userDiseaseRepository;
    EmergencyLocationMapper emergencyLocationMapper;
    EmergencySnapshotMapper emergencySnapshotMapper;
    EmergencySnapshotRepository emergencySnapshotRepository;

    @Transactional
    public void createSnapshot(Emergency emergency, EmergencyLocation location, List<UUID> symptomsIds) {
        UUID clientId = emergency.getClientId();
        log.info("Creating snapshot for emergency [{}] for client [{}]", emergency.getId(), clientId);

        List<UserAllergyDto> userAllergies = userAllergyMapper.toDtoList(userAllergyRepository.findByUserId(clientId));
        List<UserDiseaseDto> userDiseases = userDiseaseMapper.toDtoList(userDiseaseRepository.findByUserId(clientId));

        List<SymptomDto> symptoms = symptomService.collectParentsSymptomsForEmergency(emergency.getId().toString(), symptomsIds);

        EmergencySnapshot emergencySnapshot = new EmergencySnapshot()
            .setId(emergency.getId().toString())
            .setCreatedAt(emergency.getCreatedAt())
            .setStatus(emergency.getStatus())
            .setClientId(clientId)
            .setLocations(List.of(emergencyLocationMapper.toSnapshot(location)))
            .setSymptoms(symptoms)
            .setAllergies(userAllergies)
            .setDiseases(userDiseases);
        emergencySnapshotRepository.save(emergencySnapshot);
        log.debug("Snapshot for emergency [{}] saved successfully", emergency.getId());
    }

    @Transactional
    public void addSymptomsForCurrentEmergency(UpdateEmergencySymptomsRequest request, UUID clientId) {
        log.debug("Adding symptoms for client: {}", clientId);

        EmergencySnapshot emergencySnapshot = getEmergencySnapshot(clientId);

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

        EmergencySnapshot emergencySnapshot = emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)
            .orElseThrow(() -> {
                log.error(LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_CLIENT, clientId);
                return new EntityNotFoundException(EXCEPTION_NO_ONGOING_EMERGENCY_FOUND);
            });

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

        EmergencySnapshot snapshot = emergencySnapshotRepository
            .findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)
            .orElseThrow(() -> {
                log.error(LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_CLIENT, clientId);
                return new EntityNotFoundException(EXCEPTION_NO_ONGOING_EMERGENCY_FOUND);
            });

        List<SymptomDto> symptoms = snapshot.getSymptoms();
        if (symptoms == null || symptoms.isEmpty()) {
            log.info("No symptoms found in the current emergency for client: {}", clientId);
            return Collections.emptyList();
        }
        var symptomTree = emergencySnapshotMapper.buildSymptomTree(symptoms);
        log.debug("Fetched all symptoms for client: {}", clientId);
        return symptomTree;
    }

    public EmergencySnapshot getEmergencySnapshot(UUID clientId) {
        return emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)
            .orElseThrow(() -> {
                log.error(LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_CLIENT, clientId);
                return new EntityNotFoundException(EXCEPTION_NO_ONGOING_EMERGENCY_FOUND);
            });
    }

    @Transactional
    public Optional<ParamedicEmergencyViewDto> getAssignedEmergency(UUID paramedicId) {
        log.debug("Fetching assigned emergency for paramedic {}", paramedicId);

        Optional<EmergencySnapshot> emergencySnapshotOpt =
            emergencySnapshotRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING);
        if (emergencySnapshotOpt.isEmpty()) {
            log.info("No ongoing emergency assigned to paramedic {}", paramedicId);
        }
        return emergencySnapshotOpt.map(emergencySnapshotMapper::toParamedicViewDto);
    }
}
