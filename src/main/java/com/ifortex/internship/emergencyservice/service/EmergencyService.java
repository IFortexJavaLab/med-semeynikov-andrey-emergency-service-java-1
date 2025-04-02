package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.AdminCompleteEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.CancelEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.CompleteEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.ParamedicCancelEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyResolutionEntity;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyResolutionRepository;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmergencyService {

    EmergencyRepository emergencyRepository;
    EmergencySnapshotService snapshotService;
    ParamedicSearchService paramedicSearchService;
    EmergencyCommandService emergencyCommandService;
    EmergencyResolutionRepository emergencyResolutionRepository;
    ParamedicEmergencyLocationService paramedicEmergencyLocationService;

    public void createEmergency(CreateEmergencyRequest request, UserDetailsImpl client) {
        var emergency = emergencyCommandService.createEmergencyTransactional(request, client.getAccountId());
        paramedicSearchService.findParamedicForEmergency(emergency);
    }

    public void cancelAssignedEmergencyByParamedic(ParamedicCancelEmergencyRequest request, UUID paramedicId) {
        var emergency = emergencyCommandService.cancelAssignedEmergencyByParamedicTransactional(request, paramedicId);
        paramedicSearchService.findParamedicForEmergency(emergency);
    }

    @Transactional
    public void completeAssignedEmergency(CompleteEmergencyRequest request, UUID paramedicId) {
        log.info("Completing emergency for paramedic {}", paramedicId);

        Emergency emergency = emergencyRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)
            .orElseThrow(() -> {
                log.error("No active emergency found for paramedic {}", paramedicId);
                return new EntityNotFoundException("No assigned emergency found");
            });

        EmergencyResolutionEntity resolution = emergencyResolutionRepository.findById(request.emergencyResolutionId())
            .orElseThrow(() -> {
                log.error("Resolution with ID: {} not found", request.emergencyResolutionId());
                return new EntityNotFoundException("Invalid emergency resolution ID");
            });

        if (resolution.isRequiresComment() && (request.resolutionExplanation() == null || request.resolutionExplanation().isBlank())) {
            log.error("Paramedic: {} didn't provide resolution comment", paramedicId);
            throw new InvalidRequestException("This resolution requires an explanation comment");
        }

        var paramedicLocation = paramedicEmergencyLocationService.createAndSaveParamedicEmergencyLocation(
            request.longitude(), request.latitude(), paramedicId, emergency, EmergencyLocationType.FINISHED);

        emergency.setStatus(EmergencyStatus.COMPLETED)
            .setResolution(resolution)
            .setResolutionExplanation(request.resolutionExplanation());

        emergencyRepository.save(emergency);
        log.info("Emergency [{}] marked as FINISHED with resolution [{}]", emergency.getId(), resolution.getCode());

        snapshotService.updateSnapshotAfterCompletion(emergency, paramedicLocation, resolution);
    }

    @Transactional
    public void finishEmergencyByAdmin(UUID emergencyId, AdminCompleteEmergencyRequest request, UUID adminId) {
        log.info("Admin: [{}] is completing emergency [{}]", adminId, emergencyId);

        Emergency emergency = emergencyRepository.findById(emergencyId)
            .orElseThrow(() -> {
                log.error("Emergency with ID: {} not found", emergencyId);
                return new EntityNotFoundException("Emergency not found");
            });

        if (emergency.getStatus() != EmergencyStatus.ONGOING) {
            throw new InvalidRequestException("Only ongoing emergencies can be completed");
        }
        if (emergency.getParamedicId() != null) {
            throw new InvalidRequestException("Emergency already assigned to a paramedic");
        }

        EmergencyResolutionEntity resolution = emergencyResolutionRepository.findById(request.emergencyResolutionId())
            .orElseThrow(() -> new EntityNotFoundException("Resolution not found"));

        if (resolution.isRequiresComment() &&
            (request.resolutionExplanation() == null || request.resolutionExplanation().isBlank())) {
            throw new InvalidRequestException("Explanation is required for this resolution");
        }

        emergency.setStatus(EmergencyStatus.FINISHED_BY_ADMIN);
        emergency.setResolution(resolution);
        emergency.setResolutionExplanation(request.resolutionExplanation());
        emergencyRepository.save(emergency);

        log.info("Emergency {} manually marked as FINISHED_BY_ADMIN", emergencyId);

        snapshotService.updateSnapshotAfterAdminFinish(emergency, resolution);
    }

    @Transactional
    public void cancelEmergencyByClient(CancelEmergencyRequest request, UUID clientId) {
        log.info("Client [{}] is cancelling their emergency", clientId);

        Emergency emergency = emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)
            .orElseThrow(
                () -> {
                    log.error("No ongoing emergency found for client: {}", clientId);
                    return new EntityNotFoundException("No ongoing emergency found");
                }
            );

        emergency.setStatus(EmergencyStatus.CANCELLED);
        emergency.setResolutionExplanation(request != null ? request.cancellationComment() : null);
        emergencyRepository.save(emergency);

        log.info("Emergency [{}] cancelled by client [{}]", emergency.getId(), clientId);

        snapshotService.updateSnapshotAfterClientCancellation(emergency);
    }

}
