package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.ParamedicCancelEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.CancellationReasonEntity;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyAssignment;
import com.ifortex.internship.emergencyservice.repository.CancellationReasonRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyAssignmentRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmergencyCommandService {

    EmergencyRepository emergencyRepository;
    EmergencySnapshotService snapshotService;
    EmergencySnapshotService emergencySnapshotService;
    CancellationReasonRepository cancellationReasonRepository;
    EmergencyAssignmentRepository emergencyAssignmentRepository;
    ParamedicEmergencyLocationService paramedicEmergencyLocationService;

    @Transactional
    public Emergency createEmergencyTransactional(CreateEmergencyRequest request, UUID clientId) {
        boolean hasOngoingEmergencies = emergencyRepository.existsByClientIdAndStatus(clientId, EmergencyStatus.ONGOING);
        if (hasOngoingEmergencies) {
            log.error("Client [{}] already has an ongoing emergency", clientId);
            throw new InvalidRequestException("You already have an ongoing emergency.");
        }

        log.info("Creating emergency for client: {}", clientId);

        Emergency emergency = new Emergency()
            .setClientId(clientId)
            .setStatus(EmergencyStatus.ONGOING)
            .setLatitude(request.latitude())
            .setLongitude(request.longitude());
        emergency = emergencyRepository.save(emergency);

        emergencySnapshotService.createSnapshot(emergency, request.symptoms());

        log.info("Emergency [{}] created successfully", emergency.getId());

        return emergency;
    }

    public Emergency cancelAssignedEmergencyByParamedicTransactional(ParamedicCancelEmergencyRequest request, UUID paramedicId) {
        Emergency emergency = emergencyRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)
            .orElseThrow(() -> {
                log.error("No active emergency found for paramedic {}", paramedicId);
                return new EntityNotFoundException("No assigned emergency found");
            });

        EmergencyAssignment assignment = emergencyAssignmentRepository
            .findByEmergencyIdAndParamedicId(emergency.getId(), paramedicId)
            .orElseThrow(() -> {
                log.error("No assignment found for paramedic {} and emergency: {}", paramedicId, emergency.getId());
                return new EntityNotFoundException("No assignment found");
            });

        CancellationReasonEntity reason = cancellationReasonRepository.findById(request.cancellationReasonId())
            .orElseThrow(() -> {
                log.error("Cancellation reason with ID: {} not found", request.cancellationReasonId());
                return new EntityNotFoundException("Invalid cancellation reason ID");
            });

        if (reason.isRequiresComment() && (request.cancellationComment() == null || request.cancellationComment().isBlank())) {
            log.error("Paramedic: {} didn't provide cancellation comment", paramedicId);
            throw new InvalidRequestException("Cancellation comment is required");
        }

        assignment.setCanceledAt(Instant.now());
        assignment.setCancellationReason(reason);
        assignment.setCancellationComment(request.cancellationComment());

        paramedicEmergencyLocationService.createAndSaveParamedicEmergencyLocation(
            request.longitude(), request.latitude(), paramedicId, emergency, EmergencyLocationType.CANCELLED_BY_PARAMEDIC);

        snapshotService.updateSnapshotAfterCancellationByParamedic(emergency, assignment, request.latitude(), request.longitude());

        emergency.setParamedicId(null);
        emergencyRepository.save(emergency);
        log.info("Emergency {} unassigned and reassignment started", emergency.getId());
        return emergency;
    }
}
