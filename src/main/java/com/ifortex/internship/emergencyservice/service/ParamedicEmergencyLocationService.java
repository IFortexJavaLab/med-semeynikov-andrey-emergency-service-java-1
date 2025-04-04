package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.ParamedicEmergencyLocationRepository;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParamedicEmergencyLocationService {

    EmergencyRepository emergencyRepository;
    ParamedicEmergencyLocationRepository paramedicEmergencyLocationRepository;

    @Transactional
    public ParamedicEmergencyLocation createAndSaveParamedicEmergencyLocation(
        BigDecimal longitude,
        BigDecimal latitude,
        UUID paramedicId,
        Emergency emergency,
        EmergencyLocationType type) {
        log.debug("Building emergency locations for paramedic {} and emergency {}", paramedicId, emergency.getId());

        Emergency reattached = emergencyRepository.findById(emergency.getId())
            .orElseThrow(() -> {
                log.error("Emergency with ID: {} not found", emergency.getId());
                return new EntityNotFoundException("Emergency not found");
            });

        var paramedicEmergencyLocation = new ParamedicEmergencyLocation()
            .setEmergency(reattached)
            .setParamedicId(paramedicId)
            .setLocationType(type)
            .setLatitude(latitude)
            .setLongitude(longitude);

        paramedicEmergencyLocationRepository.save(paramedicEmergencyLocation);
        reattached.getParamedicLocations().add(paramedicEmergencyLocation);
        log.debug("Saved paramedic emergency location for emergency {}", emergency.getId());
        return paramedicEmergencyLocation;
    }
}
