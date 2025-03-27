package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.Symptom;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencySymptom;
import com.ifortex.internship.emergencyservice.repository.EmergencyLocationRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySymptomRepository;
import com.ifortex.internship.emergencyservice.repository.SymptomRepository;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientEmergencyService {

    SymptomRepository symptomRepository;
    EmergencyRepository emergencyRepository;
    ParamedicSearchService paramedicSearchService;
    EmergencySymptomRepository emergencySymptomRepository;
    EmergencyLocationRepository emergencyLocationRepository;

    @Transactional
    public void createEmergency(CreateEmergencyRequest request, UserDetailsImpl client) {
        UUID clientId = client.getAccountId();

        boolean hasOngoingEmergencies = emergencyRepository.existsByClientIdAndStatus(clientId, EmergencyStatus.ONGOING);
        if (hasOngoingEmergencies) {
            log.error("Client {} already has an ongoing emergency", clientId);
            throw new InvalidRequestException("You already have an ongoing emergency.");
        }

        log.info("Creating emergency for user: {}", clientId);

        Emergency emergency = new Emergency()
            .setClientId(clientId)
            .setStatus(EmergencyStatus.ONGOING);
        emergency = emergencyRepository.save(emergency);

        EmergencyLocation location = new EmergencyLocation()
            .setEmergency(emergency)
            .setLocationType(EmergencyLocationType.INITIATOR)
            .setLatitude(request.latitude())
            .setLongitude(request.longitude());

        emergencyLocationRepository.save(location);
        emergency.getLocations().add(location);

        log.debug("Emergency [{}] location set: lat={}, lon={}", emergency.getId(), location.getLatitude(), location.getLongitude());

        assignSymptomsToEmergency(emergency, request.symptoms());

        //todo create a Snapshot

        log.info("Emergency wit ID: {} created successfully", emergency.getId());

        log.info("Emergency [{}] created successfully. Initiating paramedic search...", emergency.getId());
        paramedicSearchService.findParamedicForEmergency(emergency);
    }

    private void assignSymptomsToEmergency(Emergency emergency, List<UUID> symptomIds) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            log.warn("No symptoms provided for emergency: {}", emergency.getId());
            return;
        }

        Set<UUID> uniqueSymptomIds = new HashSet<>(symptomIds);
        List<Symptom> symptoms = symptomRepository.findAllWithParentsRecursively(uniqueSymptomIds);

        for (Symptom symptom : symptoms) {
            EmergencySymptom es = new EmergencySymptom()
                .setEmergency(emergency)
                .setSymptom(symptom);
            emergencySymptomRepository.save(es);
        }

        log.debug("Assigned {} symptoms (with parents) to emergency [{}]", symptoms.size(), emergency.getId());
    }
}
