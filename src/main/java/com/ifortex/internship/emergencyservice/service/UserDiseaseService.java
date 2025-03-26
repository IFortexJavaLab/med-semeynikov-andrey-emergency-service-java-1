package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateCustomDiseaseRequest;
import com.ifortex.internship.emergencyservice.dto.request.EntityIdRequest;
import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.model.Disease;
import com.ifortex.internship.emergencyservice.model.UserDisease;
import com.ifortex.internship.emergencyservice.repository.UserDiseaseRepository;
import com.ifortex.internship.emergencyservice.util.UserDiseaseMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDiseaseService {

    public static final String LOG_DISEASE_ALREADY_ASSIGNED = "Disease: {} already assigned to user: {}";

    DiseaseService diseaseService;
    UserDiseaseMapper userDiseaseMapper;
    AuthenticationFacade authenticationFacade;
    UserDiseaseRepository userDiseaseRepository;

    public void assignDisease(EntityIdRequest request) {
        var diseaseId = request.asUUID();
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();
        log.debug("Assigning disease {} to user {}", diseaseId, userId);

        boolean alreadyAssigned = userDiseaseRepository.existsByUserIdAndDiseaseId(userId, diseaseId);
        if (alreadyAssigned) {
            log.error(LOG_DISEASE_ALREADY_ASSIGNED, diseaseId, userId);
            throw new DuplicateResourceException(String.format("Disease with ID: %s already assigned", diseaseId));
        }

        Disease disease = diseaseService.getDiseaseById(diseaseId);
        UserDisease userDisease = new UserDisease(userId, disease);
        userDiseaseRepository.save(userDisease);

        log.info("Assigned disease '{}' to user {}", disease.getName(), userId);
    }

    public void addCustomDisease(CreateCustomDiseaseRequest request) {
        String customDisease = request.name();
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();

        boolean alreadyExists = userDiseaseRepository.existsByUserIdAndCustomDiseaseIgnoreCase(userId, customDisease);
        if (alreadyExists) {
            log.error("Custom disease '{}' already assigned to user {}", customDisease, userId);
            throw new DuplicateResourceException(String.format("Custom disease: '%s' already assigned", customDisease));
        }

        UserDisease disease = new UserDisease(userId, customDisease);
        userDiseaseRepository.save(disease);

        log.info("Custom disease '{}' assigned to user {}", customDisease, userId);
    }

    @Transactional
    public void unassignDisease(EntityIdRequest request) {
        var userDiseaseId = request.asUUID();
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();

        log.debug("Unassigning user disease ID: {} for user {}", userDiseaseId, userId);
        userDiseaseRepository.deleteByUserIdAndId(userId, userDiseaseId);
        log.info("Unassigned user disease with ID: {} from user {}", userDiseaseId, userId);
    }

    public List<UserDiseaseDto> getUserDiseaseProfile() {
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();
        log.debug("Fetching disease profile for user: {}", userId);
        return userDiseaseRepository.findByUserId(userId).stream()
            .map(userDiseaseMapper::toDto)
            .toList();
    }
}