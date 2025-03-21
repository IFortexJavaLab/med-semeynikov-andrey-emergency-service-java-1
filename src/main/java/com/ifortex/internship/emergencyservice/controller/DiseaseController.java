package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.response.DiseaseDto;
import com.ifortex.internship.emergencyservice.dto.request.CreateDiseaseDto;
import com.ifortex.internship.emergencyservice.model.Disease;
import com.ifortex.internship.emergencyservice.service.DiseaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/disease")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Disease Management", description = "Operations related to managing diseases")
public class DiseaseController {

    DiseaseService diseaseService;

    @PostMapping
    @Operation(
        summary = "Create a new disease",
        description = "Creates a new disease with the provided name."
    )
    public ResponseEntity<Void> createDisease(
        @Valid @RequestBody CreateDiseaseDto createDiseaseDto
    ) {
        log.info("Request to create new disease with name: {}", createDiseaseDto.name());
        diseaseService.createDisease(createDiseaseDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(
        summary = "Get a list of diseases",
        description = "Retrieves a paginated list of diseases."
    )
    public ResponseEntity<List<DiseaseDto>> getAllDiseases(
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page can't be a negative number")
        int page,

        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "Size of the page can't be less than 1")
        @Max(value = 100, message = "Size of the page can't be more than 100")
        int size
    ) {
        log.info("Request to get diseases. Page: {}, size: {}", page, size);
        List<DiseaseDto> diseases = diseaseService.getAllDiseases(page, size);
        log.info("Retrieved diseases ({} records)", diseases.size());
        return ResponseEntity.ok(diseases);
    }

    @PutMapping
    @Operation(
        summary = "Update an existing disease",
        description = "Updates an existing disease based on the provided information."
    )
    public ResponseEntity<Disease> updateDisease(
        @Valid @RequestBody DiseaseDto updatedDisease
    ) {
        log.info("Request to update disease with ID: {}", updatedDisease.id());
        diseaseService.updateDisease(updatedDisease);
        log.info("Updated disease: {}", updatedDisease.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{diseaseId}")
    @Operation(
        summary = "Delete a disease",
        description = "Deletes a disease by its UUID."
    )
    public ResponseEntity<Void> deleteDisease(
        @PathVariable UUID diseaseId
    ) {
        diseaseService.deleteDisease(diseaseId);
        log.info("Deleted disease with ID: {}", diseaseId);
        return ResponseEntity.noContent().build();
    }
}
