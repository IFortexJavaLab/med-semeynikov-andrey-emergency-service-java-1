package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.SymptomCreate;
import com.ifortex.internship.emergencyservice.dto.request.SymptomUpdate;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.service.SymptomService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/symptom")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Symptom Management", description = "Operations related to managing symptoms")
@PreAuthorize("hasRole('ADMIN')")
public class SymptomController {

    SymptomService symptomService;

    @PostMapping
    @Operation(
        summary = "Create a new symptom",
        description = "Creates a new symptom with the provided name."
    )
    public ResponseEntity<Void> createSymptom(@Valid @RequestBody SymptomCreate symptomCreate) {
        log.info("Request to create new symptom with name: {}", symptomCreate.name());
        symptomService.createSymptom(symptomCreate);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(
        summary = "Get a list of root symptoms",
        description = "Retrieves a paginated list of the root symptoms."
    )
    public ResponseEntity<List<SymptomDto>> getAllRootSymptoms(
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page can't be a negative number")
        int page,
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "Size of the page can't be less than 1")
        @Max(value = 100, message = "Size of the page can't be more than 100")
        int size
    ) {
        log.info("Request to get root symptoms. Page: {}, size: {}", page, size);
        List<SymptomDto> symptoms = symptomService.getAllRootSymptoms(page, size);
        log.info("Retrieved symptoms ({} records)", symptoms.size());
        return ResponseEntity.ok(symptoms);
    }

    @GetMapping("/{parentId}")
    @Operation(
        summary = "Get immediate child symptoms",
        description = "Retrieves a paginated list of direct child symptoms for the specified parent symptom. "
    )
    public ResponseEntity<List<SymptomDto>> getChildSymptoms(
        @PathVariable UUID parentId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        log.info("Request to get child symptoms for parentId: {}. Page: {}, Size: {}", parentId, page, size);
        List<SymptomDto> symptoms = symptomService.getChildSymptoms(parentId, page, size);
        log.info("Retrieved child symptoms for symptom: {}. ({} records)", parentId, symptoms.size());
        return ResponseEntity.ok(symptoms);
    }

    @PutMapping
    @Operation(
        summary = "Update an existing symptom",
        description = "Updates an existing symptom based on the provided information."
    )
    public ResponseEntity<Void> updateSymptom(@Valid @RequestBody SymptomUpdate updatedSymptom) {
        log.info("Request to update symptom with ID: {}", updatedSymptom.id());
        symptomService.updateSymptom(updatedSymptom);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{symptomId}")
    @Operation(
        summary = "Delete a symptom",
        description = "Deletes a symptom and all its child symptoms recursively."
    )
    public ResponseEntity<Void> deleteSymptom(@PathVariable UUID symptomId) {
        log.info("Request to delete symptom with ID: {}", symptomId);
        symptomService.deleteSymptom(symptomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{symptomId}/potential-parents")
    @Operation(
        summary = "Get potential parent symptoms",
        description = "Retrieves a paginated list of symptoms that can be assigned as a parent for the given symptom. "
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SymptomDto>> getPotentialParents(@PathVariable UUID symptomId,
                                                                @RequestParam(defaultValue = "0")
                                                                @Min(value = 0, message = "Page can't be a negative number")
                                                                int page,
                                                                @RequestParam(defaultValue = "20")
                                                                @Min(value = 1, message = "Size of the page can't be less than 1")
                                                                @Max(value = 100, message = "Size of the page can't be more than 100")
                                                                int size) {
        log.info("Request to get potential parents for symptom: {}. Page: {}, size: {}", symptomId, page, size);
        List<SymptomDto> potentialParents = symptomService.getPotentialParents(symptomId, page, size);
        log.info("Retrieved potential parents for symptom: {}. ({} records)", symptomId, potentialParents.size());
        return ResponseEntity.ok(potentialParents);
    }
}
