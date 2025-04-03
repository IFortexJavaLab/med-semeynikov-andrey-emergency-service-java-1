package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.response.AdminEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.ClientEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyListItemDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicLocationViewDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository;
import com.ifortex.internship.emergencyservice.util.EmergencyHistoryMapper;
import com.ifortex.internship.emergencyservice.util.EmergencySnapshotMapper;
import com.ifortex.internship.medstarter.exception.custom.AuthorizationException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.ForbiddenActionException;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmergencyHistoryService {

    public static final String LOG_EMERGENCY_WITH_ID_NOT_FOUND = "Emergency with ID: {} not found";
    public static final String EXCEPTION_EMERGENCY_NOT_FOUND = "Emergency not found";
    public static final String LOG_SNAPSHOT_WITH_ID_NOT_FOUND = "Snapshot with ID: {} not found";
    public static final String EXCEPTION_SNAPSHOT_NOT_FOUND = "Snapshot not found";

    RedisService redisService;
    EmergencySnapshotMapper snapshotMapper;
    EmergencyRepository emergencyRepository;
    EmergencyHistoryMapper emergencyHistoryMapper;
    EmergencySnapshotMapper emergencySnapshotMapper;
    EmergencySnapshotRepository emergencySnapshotRepository;

    public List<EmergencyListItemDto> getEmergencyList(UserDetailsImpl user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String userRole = user.getAuthorities().stream().findFirst().orElseThrow(() ->
        {
            log.error("Miss authorities in the userDetails object");
            return new AuthorizationException("Access denied");
        }).getAuthority();

        return switch (userRole) {
            case "ROLE_CLIENT" -> getClientEmergencyList(user.getAccountId(), pageable);
            case "ROLE_PARAMEDIC" -> getParamedicEmergencyList(user.getAccountId(), pageable);
            case "ROLE_ADMIN" -> getAdminEmergencyList(pageable);
            default -> {
                log.error("Unauthorized role [{}] attempted to access list of emergencies", userRole);
                throw new ForbiddenActionException("Access denied for this role");
            }
        };
    }

    public List<EmergencyListItemDto> getClientEmergencyList(UUID clientId, Pageable pageable) {
        log.info("Fetching emergency history list for client [{}]. Page = {}, size = {}",
            clientId, pageable.getPageNumber(), pageable.getPageSize());
        return emergencyRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable).stream()
            .map(emergencyHistoryMapper::toListItemDto)
            .toList();
    }

    public List<EmergencyListItemDto> getAdminEmergencyList(Pageable pageable) {
        log.info("Fetching emergency history list for admin. Page = {}, size = {}",
            pageable.getPageNumber(), pageable.getPageSize());
        return emergencyRepository.findAll(pageable).stream()
            .map(emergencyHistoryMapper::toListItemDto)
            .toList();
    }

    public List<EmergencyListItemDto> getParamedicEmergencyList(UUID paramedicId, Pageable pageable) {
        log.info("Fetching emergency history list for paramedic [{}]. Page = {}, size = {}",
            paramedicId, pageable.getPageNumber(), pageable.getPageSize());
        return emergencyRepository.findByParamedicIdOrderByCreatedAtDesc(paramedicId, pageable).stream()
            .map(emergencyHistoryMapper::toListItemDto)
            .toList();
    }

    public ClientEmergencyViewDto getEmergencyDetailsForClient(UUID emergencyId, UUID clientId) {
        log.info("Fetching emergency details for emergency: [{}] for client: [{}]", emergencyId, clientId);

        Emergency emergency = emergencyRepository.findByIdAndClientId(emergencyId, clientId)
            .orElseThrow(() -> {
                log.error("Emergency with ID: {} for client: {} not found", emergencyId, clientId);
                return new EntityNotFoundException(EXCEPTION_EMERGENCY_NOT_FOUND);
            });

        EmergencySnapshot emergencySnapshot = emergencySnapshotRepository.findById(emergencyId.toString())
            .orElseThrow(() -> {
                log.error(LOG_SNAPSHOT_WITH_ID_NOT_FOUND, emergencyId);
                return new EntityNotFoundException(EXCEPTION_SNAPSHOT_NOT_FOUND);
            });

        ClientEmergencyViewDto emergencyDetails = snapshotMapper.toClientViewDto(emergencySnapshot);
        emergencyDetails.setLastParamedicLocation(resolveLastLocation(emergency, emergencySnapshot));

        return emergencyDetails;
    }

    //todo is it okay to send this dto? Or I should edit it for more convinient way for the frontend?
    public AdminEmergencyViewDto getEmergencyDetailsForAdmin(UUID emergencyId, UUID adminId) {
        log.info("Fetching emergency details for emergency: [{}] for admin: [{}]", emergencyId, adminId);

        Emergency emergency = emergencyRepository.findById(emergencyId)
            .orElseThrow(() -> {
                log.error(LOG_EMERGENCY_WITH_ID_NOT_FOUND, emergencyId);
                return new EntityNotFoundException(EXCEPTION_EMERGENCY_NOT_FOUND);
            });

        EmergencySnapshot emergencySnapshot = emergencySnapshotRepository.findById(emergencyId.toString())
            .orElseThrow(() -> {
                log.error(LOG_SNAPSHOT_WITH_ID_NOT_FOUND, emergencyId);
                return new EntityNotFoundException(EXCEPTION_SNAPSHOT_NOT_FOUND);
            });

        AdminEmergencyViewDto emergencyViewDto = snapshotMapper.toAdminDto(emergencySnapshot);

        if (emergency.getStatus() == EmergencyStatus.ONGOING && emergency.getParamedicId() != null) {
            var lastParamedicLocation = redisService.getLocation(emergency.getId(), emergency.getParamedicId())
                .map(location -> new ParamedicLocationViewDto(
                    location.latitude(),
                    location.longitude(),
                    location.timestamp()
                ))
                .orElse(null);
            emergencyViewDto.setCurrentParamedicLocation(lastParamedicLocation);
        }

        return emergencyViewDto;
    }

    public ParamedicLocationViewDto resolveLastLocation(Emergency emergency, EmergencySnapshot snapshot) {
        EmergencyStatus status = snapshot.getStatus();

        if (status == EmergencyStatus.ONGOING && emergency.getParamedicId() != null) {
            return redisService.getLocation(emergency.getId(), emergency.getParamedicId())
                .map(location -> new ParamedicLocationViewDto(
                    location.latitude(),
                    location.longitude(),
                    location.timestamp()
                ))
                .orElse(null);
        }

        if (snapshot.getParamedicLocations() == null || snapshot.getParamedicLocations().isEmpty()) {
            return null;
        }

        return snapshot.getParamedicLocations().stream()
            .max(Comparator.comparing(ParamedicEmergencyLocationSnapshot::getTimestamp))
            .map(location -> new ParamedicLocationViewDto(
                location.getLatitude(),
                location.getLongitude(),
                location.getTimestamp()
            ))
            .orElse(null);
    }

    public Optional<ParamedicEmergencyViewDto> getCurrentAssignedEmergency(UUID paramedicId) {
        log.debug("Fetching current assigned emergency for paramedic {}", paramedicId);

        var emergency = emergencyRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)
            .orElseThrow(
                () -> {
                    log.error("No ongoing emergency found for paramedic: {}", paramedicId);
                    return new EntityNotFoundException("No ongoing emergency found");
                }
            );

        Optional<EmergencySnapshot> emergencySnapshotOpt = emergencySnapshotRepository.findById(emergency.getId().toString());
        if (emergencySnapshotOpt.isEmpty()) {
            log.info("No ongoing emergency assigned to paramedic {}", paramedicId);
        }
        return emergencySnapshotOpt.map(emergencySnapshotMapper::toParamedicViewDtoOngoing);
    }

    public ParamedicEmergencyViewDto getEmergencyDetailsForParamedic(UUID emergencyId, UUID paramedicId) {
        log.info("Fetching emergency details for emergency: [{}] for paramedic: [{}]", emergencyId, paramedicId);

        Emergency emergency = emergencyRepository.findByIdAndParamedicId(emergencyId, paramedicId)
            .orElseThrow(() -> {
                log.error("Emergency with ID: {} for paramedic [{}] not found", emergencyId, paramedicId);
                return new EntityNotFoundException(EXCEPTION_EMERGENCY_NOT_FOUND);
            });

        EmergencySnapshot emergencySnapshot = emergencySnapshotRepository.findById(emergency.getId().toString())
            .orElseThrow(() -> {
                log.error(LOG_SNAPSHOT_WITH_ID_NOT_FOUND, emergencyId);
                return new EntityNotFoundException(EXCEPTION_SNAPSHOT_NOT_FOUND);
            });

        return emergency.getStatus().equals(EmergencyStatus.ONGOING)
            ? snapshotMapper.toParamedicViewDtoOngoing(emergencySnapshot)
            : snapshotMapper.toParamedicViewDtoFinished(emergencySnapshot);

    }
}

