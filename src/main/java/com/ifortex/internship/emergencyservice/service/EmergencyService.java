package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.AdminCompleteEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.CancelEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.CompleteEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.FeedbackRequest;
import com.ifortex.internship.emergencyservice.dto.request.ParamedicCancelEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyFeedback;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyResolutionEntity;
import com.ifortex.internship.emergencyservice.repository.EmergencyFeedbackRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyResolutionRepository;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.ForbiddenActionException;
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

    public static final String LOG_EMERGENCY_WITH_ID_NOT_FOUND = "Emergency with ID: {} not found";
    public static final String EXCEPTION_EMERGENCY_NOT_FOUND = "Emergency not found";

    EmergencyRepository emergencyRepository;
    EmergencySnapshotService snapshotService;
    ParamedicSearchService paramedicSearchService;
    EmergencyCommandService emergencyCommandService;
    EmergencyFeedbackRepository emergencyFeedbackRepository;
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
                log.error(LOG_EMERGENCY_WITH_ID_NOT_FOUND, emergencyId);
                return new EntityNotFoundException(EXCEPTION_EMERGENCY_NOT_FOUND);
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

    @Transactional
    public void leaveFeedback(UUID emergencyId, FeedbackRequest request, UUID clientId) {
        log.info("Leaving feedback from client: [{}] for emergency: [{}]", clientId, emergencyId);

        Emergency emergency = emergencyRepository.findById(emergencyId)
            .orElseThrow(() -> {
                log.error(LOG_EMERGENCY_WITH_ID_NOT_FOUND, emergencyId);
                return new EntityNotFoundException(EXCEPTION_EMERGENCY_NOT_FOUND);
            });

        boolean emergencyBelongsToClient = emergency.getClientId().equals(clientId);
        boolean feedbackAlreadyExists = emergency.getFeedback() != null;
        boolean isCompleted = emergency.getStatus().equals(EmergencyStatus.COMPLETED);
        if (!emergencyBelongsToClient) {
            log.error("Client: [{}] try to get not his own emergency: [{}]", clientId, emergencyId);
            throw new ForbiddenActionException("You can't leave feedback for this emergency");
        }

        if (!isCompleted) {
            log.error("Client: [{}] try to leave feedback to the not [{}] emergency [{}]", clientId, EmergencyStatus.COMPLETED, emergencyId);
            throw new InvalidRequestException("You can't leave feedback. Emergency isn't completed");
        }

        if (feedbackAlreadyExists) {
            log.error("Feedback already exists for emergency: [{}]", emergencyId);
            throw new InvalidRequestException("Feedback already exists for this emergency");
        }

        var feedback = new EmergencyFeedback(emergency, request.grade(), request.comment());
        feedback = emergencyFeedbackRepository.save(feedback);
        emergency.setFeedback(feedback);
        emergencyRepository.save(emergency);
        log.info("Emergency feedback created and saved for emergency: [{}] by client:[{}]. Updating snapshot...", emergencyId, clientId);

        snapshotService.updateSnapshotAfterFeedback(feedback);
    }
}
