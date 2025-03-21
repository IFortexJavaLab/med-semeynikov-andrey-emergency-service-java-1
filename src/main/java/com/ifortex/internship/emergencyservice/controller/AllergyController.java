package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.AllergyDto;
import com.ifortex.internship.emergencyservice.dto.request.CreateAllergyDto;
import com.ifortex.internship.emergencyservice.model.Allergy;
import com.ifortex.internship.emergencyservice.service.AllergyService;
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
@RequestMapping("/api/v1/allergy")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Allergy Management", description = "Operations related to managing allergies")
public class AllergyController {

    AllergyService allergyService;

    @Operation(
        summary = "Create a new allergy",
        description = "Creates a new allergy with the provided name."
    )
    @PostMapping
    public ResponseEntity<Void> createAllergy(@Valid @RequestBody CreateAllergyDto createAllergyDto) {
        log.info("Request to create new allergy with name: {}", createAllergyDto.name());
        allergyService.createAllergy(createAllergyDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(
        summary = "Get a list of allergies",
        description = "Retrieves a paginated list of allergies."
    )
    public ResponseEntity<List<AllergyDto>> getAllAllergies(
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page can't be a negative number")
        int page,
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "Size of the page can't be less than 1")
        @Max(value = 100, message = "Size of the page can't be more than 100")
        int size
    ) {
        log.info("Request to get allergies. Page: {}, size: {}", page, size);
        List<AllergyDto> allergies = allergyService.getAllAllergies(page, size);
        log.info("Retrieved allergies ({} records)", allergies.size());
        return ResponseEntity.ok(allergies);
    }

    @Operation(
        summary = "Update an existing allergy",
        description = "Updates an existing allergy based on the provided information."
    )
    @PutMapping
    public ResponseEntity<Allergy> updateAllergy(
        @Valid @RequestBody AllergyDto updatedAllergy) {
        log.info("Request to update allergy with ID: {}", updatedAllergy.id());
        allergyService.updateAllergy(updatedAllergy);
        log.info("Updated allergy: {}", updatedAllergy.name());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Delete an allergy",
        description = "Deletes an allergy by its UUID."
    )
    @DeleteMapping("/{allergyId}")
    public ResponseEntity<Void> deleteAllergy(@PathVariable UUID allergyId) {
        allergyService.deleteAllergy(allergyId);
        log.info("Deleted allergy with ID: {}", allergyId);
        return ResponseEntity.noContent().build();
    }
}
