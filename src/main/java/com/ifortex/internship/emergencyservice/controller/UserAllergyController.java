package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.CreateCustomAllergyRequest;
import com.ifortex.internship.emergencyservice.dto.request.EntityIdRequest;
import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.service.UserAllergyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/allergy")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Allergy Management", description = "Operations related to managing user's allergies")
@PreAuthorize("hasRole('CLIENT') and @subscriptionSecurity.hasActiveSubscription(authentication)")
public class UserAllergyController {

    UserAllergyService userAllergyService;

    @Operation(
        summary = "Assign existing allergy to user",
        description = "Assigns a predefined allergy to the authenticated user."
    )
    @PostMapping("/assignment")
    public ResponseEntity<Void> assignAllergy(@Valid @RequestBody EntityIdRequest request) {
        log.info("Request to assign allergy with ID: {}", request.id());
        userAllergyService.assignAllergy(request);
        log.info("Assigned allergy with ID: {}", request.id());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Add a custom allergy to user",
        description = "Adds a custom allergy provided by the user."
    )
    @PostMapping("/local")
    public ResponseEntity<Void> addCustomAllergy(@Valid @RequestBody CreateCustomAllergyRequest request) {
        log.info("Request to add custom allergy: {}", request.name());
        userAllergyService.addCustomAllergy(request);
        log.info("Custom allergy '{}' added successfully", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Unassign allergy from user",
        description = "Removes an assigned or custom allergy from the user."
    )
    @PutMapping("/unassignment")
    public ResponseEntity<Void> unassignAllergy(@Valid @RequestBody EntityIdRequest request) {
        log.info("Request to unassign user allergy with ID: {}", request.id());
        userAllergyService.unassignAllergy(request);
        log.info("Unassigned user allergy with ID: {}", request.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get user's allergy profile",
        description = "Retrieves the list of allergies currently assigned to the user, including custom ones."
    )
    @GetMapping("/profile")
    public ResponseEntity<List<UserAllergyDto>> getUserAllergyProfile() {
        List<UserAllergyDto> userAllergies = userAllergyService.getUserAllergyProfile();
        log.info("Retrieved allergy profile ({} entries)", userAllergies.size());
        return ResponseEntity.ok(userAllergies);
    }
}
