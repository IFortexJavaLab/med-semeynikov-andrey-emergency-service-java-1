package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.dto.DiseaseDto;
import com.ifortex.internship.emergencyservice.dto.request.CreateDiseaseDto;
import com.ifortex.internship.emergencyservice.model.Disease;
import com.ifortex.internship.emergencyservice.repository.DiseaseRepository;
import com.ifortex.internship.emergencyservice.util.DiseaseMapper;
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
public class DiseaseService {

    public static final String LOG_DISEASE_WITH_NAME_IS_ALREADY_EXISTS = "Disease with name : {} is already exists";
    public static final String LOG_DISEASE_NAME_IS_NULL_OR_BLANK = "Disease name is null or blank";

    DiseaseRepository diseaseRepository;
    DiseaseMapper diseaseMapper;

    public void createDisease(CreateDiseaseDto createDiseaseDto) {
        String name = createDiseaseDto.name();

        if (name == null || name.isBlank()) {
            log.error(LOG_DISEASE_NAME_IS_NULL_OR_BLANK);
            throw new InvalidRequestException("Disease name must not be null or blank");
        }
        log.debug("Creating new disease with name: {}", name);

        if (diseaseRepository.findByName(name).isPresent()) {
            log.error(LOG_DISEASE_WITH_NAME_IS_ALREADY_EXISTS, name);
            throw new DuplicateResourceException(String.format("Disease with name %s already exists.", name));
        }
        Disease disease = new Disease(name);
        diseaseRepository.save(disease);
        log.info("Disease with name: {} and ID: {} created successfully", name, disease.getId());
    }

    public List<DiseaseDto> getAllDiseases(int page, int size) {
        log.debug("Getting list of diseases. Page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Disease> diseases = diseaseRepository.findAll(pageable);
        return diseaseMapper.toListDtos(diseases.stream().toList());
    }

    public void updateDisease(DiseaseDto disease) {
        log.debug("Attempt to update disease with ID: {}", disease.id());

        if (disease.name() == null || disease.name().isBlank()) {
            log.error(LOG_DISEASE_NAME_IS_NULL_OR_BLANK);
            throw new InvalidRequestException(String.format("Disease name must not be null or blank. Disease with ID: %s", disease.id()));
        }

        if (diseaseRepository.findByName(disease.name()).isPresent()) {
            log.error(LOG_DISEASE_WITH_NAME_IS_ALREADY_EXISTS, disease.name());
            throw new DuplicateResourceException(String.format("Disease with name %s already exists.", disease.name()));
        }
        Disease existingDisease = getDiseaseById(UUID.fromString(disease.id()));
        existingDisease.setName(disease.name());

        diseaseRepository.save(existingDisease);
        log.info("Disease with ID: {} updated successfully", disease.id());
    }

    public void deleteDisease(UUID diseaseId) {
        log.debug("Attempt to delete disease with ID: {}", diseaseId);
        Disease disease = getDiseaseById(diseaseId);
        diseaseRepository.delete(disease);
        log.info("Disease with ID: {} deleted successfully", diseaseId);
    }

    public Disease getDiseaseById(UUID diseaseId) {
        log.info("Request to get disease by ID: {}", diseaseId);
        return diseaseRepository.findById(diseaseId)
            .orElseThrow(() -> {
                log.error("Disease with ID {} not found", diseaseId);
                return new EntityNotFoundException(String.format("Disease with ID: %s not found", diseaseId));
            });
    }
}
