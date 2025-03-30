package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import com.ifortex.internship.emergencyservice.repository.EmergencyLocationRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
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
    ParamedicSearchService paramedicSearchService;
    EmergencySnapshotService emergencySnapshotService;
    EmergencyLocationRepository emergencyLocationRepository;

    @Transactional
    public void createEmergency(CreateEmergencyRequest request, UserDetailsImpl client) {
        UUID clientId = client.getAccountId();

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

        ParamedicEmergencyLocation location = new ParamedicEmergencyLocation()
            .setEmergency(emergency)
            .setLocationType(EmergencyLocationType.ACCEPTED)
            .setLatitude(request.latitude())
            .setLongitude(request.longitude());

        emergencyLocationRepository.save(location);
        emergency.getParamedicLocations().add(location);

        log.debug("Emergency [{}] location set: lat={}, lon={}", emergency.getId(), location.getLatitude(), location.getLongitude());

        emergencySnapshotService.createSnapshot(emergency, location, request.symptoms());

        log.info("Emergency [{}] created successfully. Initiating paramedic search...", emergency.getId());
        paramedicSearchService.findParamedicForEmergency(emergency);
    }
}
