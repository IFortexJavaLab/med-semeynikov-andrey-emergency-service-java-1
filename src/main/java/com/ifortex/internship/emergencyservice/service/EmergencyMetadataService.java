package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.response.CancellationReasonDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyResolutionDto;
import com.ifortex.internship.emergencyservice.repository.CancellationReasonRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyResolutionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmergencyMetadataService {

    EmergencyResolutionRepository emergencyResolutionRepository;
    CancellationReasonRepository cancellationReasonRepository;

    public List<EmergencyResolutionDto> getAllEmergencyResolutions() {
        List<EmergencyResolutionDto> result = emergencyResolutionRepository.findAll().stream()
            .map(r -> new EmergencyResolutionDto(r.getId(), r.getCode().toString(), r.getDescription(), r.isRequiresComment()))
            .toList();
        log.debug("Loaded {} emergency resolutions", result.size());
        return result;
    }

    public List<CancellationReasonDto> getAllCancellationReasons() {
        List<CancellationReasonDto> result = cancellationReasonRepository.findAll().stream()
            .map(r -> new CancellationReasonDto(r.getId(), r.getCode().toString(), r.getDescription(), r.isRequiresComment()))
            .toList();
        log.debug("Loaded {} cancellation reasons", result.size());
        return result;
    }
}
