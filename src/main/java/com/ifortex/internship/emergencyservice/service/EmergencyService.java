package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
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
import com.ifortex.internship.emergencyservice.util.EmergencySymptomMapper;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
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
public class EmergencyService {

    public static final String LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_USER = "No ongoing emergency found for user: {}";

    SymptomRepository symptomRepository;
    EmergencyRepository emergencyRepository;
    ParamedicSearchService paramedicSearchService;
    EmergencySymptomMapper emergencySymptomMapper;
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

        log.info("Emergency [{}] created successfully. Initiating paramedic search...", emergency.getId());
        paramedicSearchService.findParamedicForEmergency(emergency);
    }

    public List<EmergencySymptomListDto> getSymptomsForCurrentEmergency(UserDetailsImpl client) {
        UUID clientId = client.getAccountId();
        log.debug("Fetching current emergency symptoms for user: {}", clientId);

        //todo call to mongo instead of postgres

        // log.debug("Found {} symptoms in emergency [{}]", emergencySymptoms.size(), currentEmergency.getId());*/

        //todo map symptoms from mongo to get EmergencySymptomListDto

        return null;
    }

    @Transactional
    public void addSymptomsForCurrentEmergency(UpdateEmergencySymptomsRequest request, UserDetailsImpl client) {
        UUID clientId = client.getAccountId();
        log.debug("Adding symptoms for user: {}", clientId);

        Emergency emergency = emergencyRepository
            .findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)
            .orElseThrow(() -> {
                log.warn(LOG_NO_ONGOING_EMERGENCY_FOUND_FOR_USER, clientId);
                return new EntityNotFoundException("No ongoing emergency found");
            });

        assignSymptomsToEmergency(emergency, request.symptoms());

        //todo update shapshot in the mongo
    }

    @Transactional
    public EmergencyDto getAssignedEmergency(UUID paramedicId) {
        log.debug("Fetching assigned emergency for paramedic {}", paramedicId);

        //todo call to mongo

        //todo map results from mongo to EmergencyDto
        return null;
    }

    private void assignSymptomsToEmergency(Emergency emergency, List<UUID> symptomIds) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            log.warn("No symptoms provided for emergency: {}", emergency.getId());
            return;
        }

        Set<UUID> uniqueSymptomIds = new HashSet<>(symptomIds);
        List<Symptom> symptoms = symptomRepository.findAllWithParentsRecursively(uniqueSymptomIds);

        List<EmergencySymptom>
            emergencySymptoms =
            symptoms.stream()
                .map(symptom -> new EmergencySymptom()
                    .setEmergency(emergency)
                    .setSymptom(symptom))
                .toList();
        emergencySymptomRepository.saveAll(emergencySymptoms);

        log.debug("Assigned {} symptoms (with parents) to emergency [{}]", symptoms.size(), emergency.getId());
    }
}
