package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.model.ParamedicLocation;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyAssignment;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyAssignmentSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import com.ifortex.internship.emergencyservice.repository.EmergencyAssignmentRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyLocationRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository;
import com.ifortex.internship.emergencyservice.repository.ParamedicLocationRepository;
import com.ifortex.internship.emergencyservice.util.EmergencyAssignmentMapper;
import com.ifortex.internship.emergencyservice.util.EmergencyLocationMapper;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InternalServiceException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParamedicSearchService {

    static final int MAX_ATTEMPTS = 3;
    static final Duration BASE_DELAY = Duration.ofMinutes(1);
    static final Duration EXTENDED_SEARCH_DURATION = Duration.ofMinutes(20);

    EmergencyRepository emergencyRepository;
    EmergencyLocationMapper emergencyLocationMapper;
    EmergencyAssignmentMapper emergencyAssignmentMapper;
    EmergencySnapshotRepository emergencySnapshotRepository;
    ParamedicLocationRepository paramedicLocationRepository;
    EmergencyLocationRepository emergencyLocationRepository;
    EmergencyAssignmentRepository emergencyAssignmentRepository;

    @Value("${app.default_radius_km}") double defaultRadius;

    @Async
    @Transactional
    public void findParamedicForEmergency(Emergency emergency) {
        BigDecimal latitude = getLatitude(emergency);
        BigDecimal longitude = getLongitude(emergency);

        log.info("Starting paramedic search for emergency [{}], location: ({}, {})", emergency.getId(), latitude, longitude);

        double radius = defaultRadius;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            log.debug("Attempt {}: searching paramedic within radius {} km", i + 1, radius);
            Optional<ParamedicLocation> found = paramedicLocationRepository.findNearestAvailableParamedicInRadius(latitude, longitude, radius);
            if (found.isPresent()) {
                log.info("Paramedic {} found on attempt {} within radius {}", found.get().getParamedicId(), i + 1, radius);
                assign(found.get(), emergency);
                return;
            }
            log.debug("No paramedic found on attempt {}. Retrying after delay...", i + 1);
            sleep(BASE_DELAY);
        }

        radius *= 2;
        Instant timeout = Instant.now().plus(EXTENDED_SEARCH_DURATION);
        log.info("Switching to extended search. Radius increased to {}. Emergency [{}]", radius, emergency.getId());

        while (Instant.now().isBefore(timeout)) {
            log.debug("Extended search: trying to find paramedic within radius {} km. Emergency [{}]", radius, emergency.getId());
            Optional<ParamedicLocation> found = paramedicLocationRepository.findNearestAvailableParamedicInRadius(latitude, longitude, radius);
            if (found.isPresent()) {
                log.info("Paramedic {} found during extended search", found.get().getParamedicId());
                assign(found.get(), emergency);
                return;
            }
            sleep(BASE_DELAY);
        }

        emergency.setStatus(EmergencyStatus.RESERVE_HANDLED);
        emergencyRepository.save(emergency);

        var emergencySnapshot = emergencySnapshotRepository.findById(emergency.getId().toString())
            .orElseThrow(() -> {
                log.error("Emergency [{}] not found", emergency.getId());
                return new EntityNotFoundException(String.format("Emergency [%s] not found", emergency.getId()));
            });

        emergencySnapshot.setStatus(emergency.getStatus()).setClosedAt(Instant.now());
        emergencySnapshotRepository.save(emergencySnapshot);

        // todo notificationService.notifyReserveTeam(emergency);

        log.info("Emergency [{}] resolved by reserve team. No paramedic found in {} minutes", emergency.getId(),
            EXTENDED_SEARCH_DURATION.toMinutes());
    }

    private void assign(ParamedicLocation paramedicLocation, Emergency emergency) {
        UUID paramedicId = paramedicLocation.getParamedicId();
        UUID emergencyId = emergency.getId();
        log.info("Assigning paramedic {} to emergency {}", paramedicId, emergencyId);

        updateEmergencyWithParamedic(paramedicLocation, emergency);
        EmergencyAssignment assignment = createAndSaveAssignment(paramedicLocation, emergency);
        List<EmergencyLocation> emergencyLocations = createAndSaveEmergencyLocations(paramedicLocation, emergency);
        updateEmergencySnapshot(emergency, assignment, emergencyLocations);

        //todo: notificationService.notifyParamedic(paramedicId, emergency);

        log.info("Paramedic {} assigned to emergency {} with accepted and current location saved", paramedicId, emergencyId);
    }

    private void updateEmergencyWithParamedic(ParamedicLocation paramedicLocation, Emergency emergency) {
        log.debug("Deleting existing PARAMEDIC_CURRENT locations for emergency {}", emergency.getId());
        emergencyLocationRepository.deleteByEmergencyIdAndLocationType(emergency.getId(), EmergencyLocationType.PARAMEDIC_CURRENT);

        emergency.setParamedicId(paramedicLocation.getParamedicId());
        emergencyRepository.save(emergency);
        log.debug("Updated emergency {} with paramedic {}", emergency.getId(), paramedicLocation.getParamedicId());
    }

    private EmergencyAssignment createAndSaveAssignment(ParamedicLocation paramedicLocation, Emergency emergency) {
        log.debug("Creating assignment for paramedic {} and emergency {}", paramedicLocation.getParamedicId(), emergency.getId());
        EmergencyAssignment assignment = new EmergencyAssignment()
            .setParamedicId(paramedicLocation.getParamedicId())
            .setEmergency(emergency);
        emergencyAssignmentRepository.save(assignment);
        log.debug("Assignment created for emergency {} with id {}", emergency.getId(), assignment.getId());
        return assignment;
    }

    private List<EmergencyLocation> createAndSaveEmergencyLocations(ParamedicLocation paramedicLocation, Emergency emergency) {
        log.debug("Building emergency locations for paramedic {} and emergency {}", paramedicLocation.getParamedicId(), emergency.getId());
        List<EmergencyLocation> emergencyLocations = List.of(
            buildEmergencyLocation(emergency, paramedicLocation, EmergencyLocationType.PARAMEDIC_ACCEPTED),
            buildEmergencyLocation(emergency, paramedicLocation, EmergencyLocationType.PARAMEDIC_CURRENT)
        );
        emergencyLocationRepository.saveAll(emergencyLocations);
        log.debug("Saved {} emergency locations for emergency {}", emergencyLocations.size(), emergency.getId());
        return emergencyLocations;
    }

    private void updateEmergencySnapshot(Emergency emergency, EmergencyAssignment assignment, List<EmergencyLocation> emergencyLocations) {
        log.debug("Fetching snapshot for emergency {}", emergency.getId());
        EmergencySnapshot snapshot = emergencySnapshotRepository.findById(emergency.getId().toString())
            .orElseThrow(() -> {
                log.error("Emergency [{}] snapshot not found", emergency.getId());
                return new EntityNotFoundException("Emergency snapshot not found");
            });
        log.debug("Updating snapshot for emergency {} with paramedic {}", emergency.getId(), emergency.getParamedicId());
        snapshot.setParamedicId(emergency.getParamedicId());

        EmergencyAssignmentSnapshot assignmentSnapshot = emergencyAssignmentMapper.toSnapshot(assignment);
        if (snapshot.getAssignments() == null) {
            snapshot.setAssignments(new ArrayList<>());
        }
        snapshot.getAssignments().add(assignmentSnapshot);
        log.debug("Added assignment snapshot for paramedic {} to emergency {}", assignment.getParamedicId(), emergency.getId());
        snapshot.getLocations().removeIf(location -> location.getLocationType() == EmergencyLocationType.PARAMEDIC_CURRENT);

        var snapshotLocations = emergencyLocationMapper.toList(emergencyLocations);
        snapshot.getLocations().addAll(snapshotLocations);

        emergencySnapshotRepository.save(snapshot);
        log.debug("Snapshot for emergency {} updated successfully", emergency.getId());
    }

    private EmergencyLocation buildEmergencyLocation(Emergency emergency,
                                                     ParamedicLocation location,
                                                     EmergencyLocationType type) {
        return new EmergencyLocation()
            .setEmergency(emergency)
            .setLocationType(type)
            .setLatitude(location.getLatitude())
            .setLongitude(location.getLongitude());
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private BigDecimal getLatitude(Emergency emergency) {
        return emergency.getLocations().stream()
            .filter(loc -> loc.getLocationType() == EmergencyLocationType.INITIATOR)
            .findFirst()
            .map(EmergencyLocation::getLatitude)
            .orElseThrow(() -> {
                log.error("Latitude not found for emergency {}", emergency.getId());
                return new InternalServiceException(
                    String.format("User location not found in the emergency object with ID: %s", emergency.getId()));
            });
    }

    private BigDecimal getLongitude(Emergency emergency) {
        return emergency.getLocations().stream()
            .filter(loc -> loc.getLocationType() == EmergencyLocationType.INITIATOR)
            .findFirst()
            .map(EmergencyLocation::getLongitude)
            .orElseThrow(() -> {
                log.error("Longitude not found for emergency {}", emergency.getId());
                return new InternalServiceException("User location not found in emergency");
            });
    }
}