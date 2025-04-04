package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.UpdateParamedicLocationRequest;
import com.ifortex.internship.emergencyservice.model.ParamedicLocation;
import com.ifortex.internship.emergencyservice.repository.ParamedicLocationRepository;
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
public class ParamedicLocationService {

    ParamedicLocationRepository locationRepository;

    public void updateLocation(UpdateParamedicLocationRequest request, UUID paramedicId) {

        //todo there can call to accounting to get medic's profile and get name

        var locationOpt = locationRepository.findById(paramedicId);
        ParamedicLocation location;
        if (locationOpt.isPresent()) {
            location = locationOpt.get();
            location.setLongitude(request.longitude());
            location.setLatitude(request.latitude());
        } else {
            location = ParamedicLocation.builder()
                .paramedicId(paramedicId)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        }

        locationRepository.save(location);
        log.info("Updated location for paramedic [{}]: lat={}, lng={}",
            paramedicId, request.latitude(), request.longitude());
    }
}
