package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.Symptom;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencySymptom;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySymptomRepository;
import com.ifortex.internship.emergencyservice.repository.LocationRepository;
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
    LocationRepository locationRepository;
    EmergencyRepository emergencyRepository;
    EmergencySymptomRepository emergencySymptomRepository;

    @Transactional
    public void createEmergency(CreateEmergencyRequest request, UserDetailsImpl client) {
        UUID clientAccountId = client.getAccountId();

        boolean hasOngoingEmergencies = emergencyRepository.existsByClientIdAndStatus(clientAccountId, EmergencyStatus.ONGOING);
        if (hasOngoingEmergencies) {
            log.error("Client with ID: {} tries to create another emergency while has emergency with status: {}",
                clientAccountId,
                EmergencyStatus.ONGOING);
            throw new InvalidRequestException("You already have an ongoing emergency.");
        }

        log.debug("Creating emergency for user: {}", clientAccountId);

        Emergency emergency = new Emergency();
        emergency.setClientId(clientAccountId);
        emergency.setStatus(EmergencyStatus.ONGOING);
        emergency = emergencyRepository.save(emergency);

        EmergencyLocation location = new EmergencyLocation();
        location.setEmergency(emergency);
        location.setLocationType(EmergencyLocationType.INITIATOR);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        locationRepository.save(location);

        Set<UUID> uniqueSymptoms = new HashSet<>(request.symptoms());
        List<Symptom> symptomsWithParents = symptomRepository.findAllWithParentsRecursively(uniqueSymptoms);

        for (Symptom symptom : symptomsWithParents) {
            EmergencySymptom es = new EmergencySymptom();
            es.setEmergency(emergency);
            es.setSymptom(symptom);
            emergencySymptomRepository.save(es);
        }

        //todo create Snapshot

        log.info("Emergency [{}] created with {} symptoms", emergency.getId(), symptomsWithParents.size());
        // todo initiate search
    }
}
