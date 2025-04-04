package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.model.ParamedicLocation;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyAssignment;
import com.ifortex.internship.emergencyservice.repository.EmergencyAssignmentRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository;
import com.ifortex.internship.emergencyservice.repository.ParamedicLocationRepository;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParamedicSearchService {

    EmergencyRepository emergencyRepository;
    EmergencySnapshotService snapshotService;

    EmergencySnapshotRepository emergencySnapshotRepository;
    ParamedicLocationRepository paramedicLocationRepository;
    EmergencyAssignmentRepository emergencyAssignmentRepository;

    @Value("${app.emergency.max_attempts}") int maxAttempts;
    @Value("${app.emergency.default_radius_km}") double defaultRadius;
    @Value("#{T(java.time.Duration).ofMinutes(T(java.lang.Long).parseLong('${app.emergency.base_delay_minutes}'))}")
    Duration baseDelay;
    @Value("#{T(java.time.Duration).ofMinutes(T(java.lang.Long).parseLong('${app.emergency.extended_search_duration_minutes}'))}")
    Duration extendedSearchDuration;
    private final ParamedicEmergencyLocationService paramedicEmergencyLocationService;

    @Async
    public void findParamedicForEmergency(Emergency emergency) {
        BigDecimal latitude = emergency.getLatitude();
        BigDecimal longitude = emergency.getLongitude();
        UUID emergencyId = emergency.getId();

        log.info("Starting paramedic search for emergency [{}], location: ({}, {})", emergency.getId(), latitude, longitude);

        double radius = defaultRadius;
        for (int i = 0; i < maxAttempts; i++) {
            if (shouldAbort(emergencyId)) {
                log.info("Aborting search: emergency [{}] is no longer active", emergencyId);
                return;
            }
            log.debug("Attempt {}: searching paramedic within radius {} km", i + 1, radius);
            Optional<ParamedicLocation>
                found =
                paramedicLocationRepository.findNearestAvailableParamedicInRadius(latitude, longitude, radius, emergency.getId());

            if (found.isPresent()) {
                log.info("Paramedic {} found on attempt {} within radius {}", found.get().getParamedicId(), i + 1, radius);
                assign(found.get(), emergency);
                return;
            }
            log.debug("No paramedic found on attempt {}. Retrying after delay...", i + 1);
            sleep(baseDelay);
        }

        radius *= 2;
        Instant timeout = Instant.now().plus(extendedSearchDuration);
        log.info("Switching to extended search. Radius increased to {}. Emergency [{}]", radius, emergency.getId());

        while (Instant.now().isBefore(timeout)) {
            if (shouldAbort(emergencyId)) {
                log.warn("Aborting extended search: emergency [{}] is no longer active", emergencyId);
                return;
            }

            log.debug("Extended search: trying to find paramedic within radius {} km. Emergency [{}]", radius, emergency.getId());
            Optional<ParamedicLocation>
                found =
                paramedicLocationRepository.findNearestAvailableParamedicInRadius(latitude, longitude, radius, emergency.getId());
            if (found.isPresent()) {
                log.info("Paramedic {} found during extended search", found.get().getParamedicId());
                assign(found.get(), emergency);
                return;
            }
            sleep(baseDelay);
        }

        if (!shouldAbort(emergencyId)) {
            emergency.setStatus(EmergencyStatus.RESERVE_HANDLED);
            emergencyRepository.save(emergency);

            var emergencySnapshot = emergencySnapshotRepository.findById(emergency.getId().toString())
                .orElseThrow(() -> {
                    log.error("Emergency [{}] not found", emergency.getId());
                    return new EntityNotFoundException(String.format("Emergency [%s] not found", emergency.getId()));
                });

            emergencySnapshot.setStatus(emergency.getStatus()).setClosedAt(Instant.now());
            emergencySnapshotRepository.save(emergencySnapshot);

            log.info("MOCK. Sent request to reserve team service");

            log.info("Emergency [{}] resolved by reserve team. No paramedic found in {} minutes", emergency.getId(),
                extendedSearchDuration.toMinutes());
        } else {
            log.warn("Skipping reserve update: emergency [{}] already completed", emergencyId);
        }
    }

    private void assign(ParamedicLocation paramedicLocation, Emergency emergency) {
        UUID paramedicId = paramedicLocation.getParamedicId();
        UUID emergencyId = emergency.getId();
        log.info("Assigning paramedic {} to emergency {}", paramedicId, emergencyId);

        EmergencyAssignment assignment = createAndSaveAssignment(paramedicLocation, emergency);

        EmergencyLocationType type = EmergencyLocationType.ACCEPTED;
        var paramedicEmergencyLocation =
            paramedicEmergencyLocationService.createAndSaveParamedicEmergencyLocation(
                paramedicLocation.getLongitude(),
                paramedicLocation.getLatitude(),
                paramedicId, emergency, type);

        emergency.setParamedicId(paramedicId);
        emergencyRepository.save(emergency);
        snapshotService.updateEmergencySnapshotAfterParamedicAssign(
            emergency, assignment,
            paramedicEmergencyLocation,
            paramedicLocation.getParamedicFirstName());

        log.info("MOCK. Notify paramedic: [{}] about assigned emergency", paramedicId);

        log.info("Paramedic {} assigned to emergency {} with accepted and current location saved", paramedicId, emergencyId);
    }

    private EmergencyAssignment createAndSaveAssignment(ParamedicLocation paramedicLocation, Emergency emergency) {
        log.debug("Creating assignment for paramedic {} and emergency {}", paramedicLocation.getParamedicId(), emergency.getId());
        EmergencyAssignment assignment = EmergencyAssignment.builder()
            .paramedicId(paramedicLocation.getParamedicId())
            .emergency(emergency).build();
        emergencyAssignmentRepository.save(assignment);
        log.debug("Assignment created for emergency {} with id {}", emergency.getId(), assignment.getId());
        return assignment;
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean shouldAbort(UUID emergencyId) {
        return emergencyRepository.findById(emergencyId)
            .map(e -> e.getStatus() != EmergencyStatus.ONGOING)
            .orElse(true);
    }
}