package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.AllergyDto;
import com.ifortex.internship.emergencyservice.dto.request.CreateAllergyDto;
import com.ifortex.internship.emergencyservice.model.Allergy;
import com.ifortex.internship.emergencyservice.repository.AllergyRepository;
import com.ifortex.internship.emergencyservice.util.AllergyMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AllergyService {

    public static final String LOG_ALLERGY_WITH_NAME_IS_ALREADY_EXISTS = "Allergy with name : {} is already exists";
    public static final String LOG_ALLERGY_NAME_IS_NULL_OR_BLANK = "Allergy name is null or blank";

    AllergyRepository allergyRepository;
    AllergyMapper allergyMapper;

    public void createAllergy(CreateAllergyDto allergyDto) {
        String name = allergyDto.name();

        if (name == null || name.isBlank()) {
            log.error(LOG_ALLERGY_NAME_IS_NULL_OR_BLANK);
            throw new InvalidRequestException("Allergy name must not be null or blank");
        }
        log.debug("Creating new allergy with name: {}", name);

        if (allergyRepository.findByName(name).isPresent()) {
            log.error(LOG_ALLERGY_WITH_NAME_IS_ALREADY_EXISTS, name);
            throw new DuplicateResourceException(String.format("Allergy with name %s already exists.", name));
        }
        Allergy allergy = new Allergy(name);
        allergyRepository.save(allergy);
        log.info("Allergy with name: {} and ID: {} created successfully", name, allergy.getId());
    }

    public List<AllergyDto> getAllAllergies(int page, int size) {
        log.debug("Getting list of allergies. Page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Allergy> allergies = allergyRepository.findAll(pageable);
        return allergyMapper.toListDtos(allergies.stream().toList());
    }

    public void updateAllergy(AllergyDto allergy) {
        log.debug("Attempt to update allergy with ID: {}", allergy.id());

        if (allergy.name() == null || allergy.name().isBlank()) {
            log.error(LOG_ALLERGY_NAME_IS_NULL_OR_BLANK);
            throw new InvalidRequestException(String.format("Allergy name must not be null or blank. Allergy ID: %s", allergy.id()));
        }

        if (allergyRepository.findByName(allergy.name()).isPresent()) {
            log.error(LOG_ALLERGY_WITH_NAME_IS_ALREADY_EXISTS, allergy.name());
            throw new DuplicateResourceException(String.format("Allergy with name %s already exists.", allergy.name()));
        }

        Allergy existingAllergy = getAllergyById(UUID.fromString(allergy.id()));
        existingAllergy.setName(allergy.name());

        allergyRepository.save(existingAllergy);
        log.info("Allergy with ID: {} updated successfully", allergy.name());
    }

    public void deleteAllergy(UUID allergyId) {
        log.debug("Attempt to delete allergy with ID: {}", allergyId);
        Allergy allergy = getAllergyById(allergyId);
        allergyRepository.delete(allergy);
        log.info("Allergy with ID: {} deleted successfully", allergyId);
    }

    public Allergy getAllergyById(UUID allergyId) {
        log.info("Request to get allergy by ID: {}", allergyId);
        return allergyRepository.findById(allergyId)
            .orElseThrow(() -> {
                log.error("Allergy with ID {} not found", allergyId);
                return new EntityNotFoundException(String.format("Allergy with ID: %s not found", allergyId));
            });
    }
}
