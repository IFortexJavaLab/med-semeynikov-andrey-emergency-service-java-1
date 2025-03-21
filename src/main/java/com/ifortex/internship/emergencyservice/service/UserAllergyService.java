package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.request.AllergyIdRequest;
import com.ifortex.internship.emergencyservice.dto.request.CreateCustomAllergyRequest;
import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.model.Allergy;
import com.ifortex.internship.emergencyservice.model.UserAllergy;
import com.ifortex.internship.emergencyservice.repository.UserAllergyRepository;
import com.ifortex.internship.emergencyservice.util.UserAllergyMapper;
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
public class UserAllergyService {

    public static final String LOG_ALLERGY_ALREADY_ASSIGNED = "Allergy: {} already assigned to user: {}";

    AllergyService allergyService;
    UserAllergyMapper userAllergyMapper;
    AuthenticationFacade authenticationFacade;
    UserAllergyRepository userAllergyRepository;

    public void assignAllergy(AllergyIdRequest request) {
        var allergyId = request.asUUID();
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();
        log.debug("Assigning allergy {} to user {}", allergyId, userId);

        Allergy allergy = allergyService.getAllergyById(allergyId);

        boolean alreadyAssigned = userAllergyRepository.existsByUserIdAndAllergyId(userId, allergyId);
        if (alreadyAssigned) {
            log.error(LOG_ALLERGY_ALREADY_ASSIGNED, allergyId, userId);
            throw new DuplicateResourceException(String.format("Allergy: %s already assigned", allergy.getName()));
        }

        UserAllergy userAllergy = new UserAllergy(userId, allergy);
        userAllergyRepository.save(userAllergy);

        log.info("Assigned allergy '{}' to user {}", allergy.getName(), userId);
    }

    public void addCustomAllergy(CreateCustomAllergyRequest request) {
        String customAllergy = request.name();
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();

        boolean alreadyExists = userAllergyRepository.existsByUserIdAndCustomAllergyIgnoreCase(userId, customAllergy);
        if (alreadyExists) {
            log.error("Custom allergy '{}' already assigned to user {}", customAllergy, userId);
            throw new DuplicateResourceException(String.format("Custom allergy: '%s' already assigned", customAllergy));
        }

        UserAllergy allergy = new UserAllergy(userId, customAllergy);
        userAllergyRepository.save(allergy);

        log.info("Custom allergy '{}' assigned to user {}", customAllergy, userId);
    }

    @Transactional
    public void unassignAllergy(AllergyIdRequest request) {
        var userAllergyId = request.asUUID();
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();

        log.debug("Unassigning user allergy ID: {} for user {}", userAllergyId, userId);
        userAllergyRepository.deleteByUserIdAndId(userId, userAllergyId);
        log.info("Unassigned user allergy with ID: {} from user {}", userAllergyId, userId);
    }

    public List<UserAllergyDto> getUserAllergyProfile() {
        UUID userId = authenticationFacade.getAccountIdFromAuthentication();
        log.debug("Fetching allergy profile for user: {}", userId);
        return userAllergyRepository.findByUserId(userId).stream()
            .map(userAllergyMapper::toDto)
            .toList();
    }
}
