package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.CreateCustomDiseaseRequest;
import com.ifortex.internship.emergencyservice.dto.request.EntityIdRequest;
import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.service.UserDiseaseService;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
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
@RequestMapping("/api/v1/disease")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Disease Management", description = "Operations related to managing user's diseases")
@PreAuthorize("hasRole('CLIENT') and @subscriptionSecurity.hasActiveSubscription(authentication)")
public class UserDiseaseController {

    UserDiseaseService userDiseaseService;
    AuthenticationFacade authenticationFacade;

    @Operation(
        summary = "Assign existing disease to user",
        description = "Assigns a predefined disease to the authenticated user."
    )
    @PostMapping("/assignment")
    public ResponseEntity<Void> assignDisease(@Valid @RequestBody EntityIdRequest request) {
        log.info("Request to assign disease with ID: {}", request.id());
        userDiseaseService.assignDisease(request);
        log.info("Assigned disease with ID: {}", request.id());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Add a custom disease to user", description = "Adds a custom disease provided by the user.")
    @PostMapping("/local")
    public ResponseEntity<Void> addCustomDisease(@Valid @RequestBody CreateCustomDiseaseRequest request) {
        log.info("Request to add custom disease: {}", request.name());
        userDiseaseService.addCustomDisease(request);
        log.info("Custom disease '{}' added successfully", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Unassign disease from user", description = "Removes an assigned or custom disease from the user.")
    @PutMapping("/unassignment")
    public ResponseEntity<Void> unassignDisease(@Valid @RequestBody EntityIdRequest request) {
        log.info("Request to unassign user disease with ID: {}", request.id());
        userDiseaseService.unassignDisease(request);
        log.info("Unassigned user disease with ID: {}", request.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user's disease profile", description = "Retrieves the list of diseases currently assigned to the user, including custom ones.")
    @GetMapping("/profile")
    public ResponseEntity<List<UserDiseaseDto>> getUserDiseaseProfile() {
        var userId = authenticationFacade.getAccountIdFromAuthentication();
        log.info("Request to get user disease profile for user: {}", userId);
        List<UserDiseaseDto> userDiseases = userDiseaseService.getUserDiseaseProfile();
        log.info("Retrieved disease profile ({} entries)", userDiseases.size());
        return ResponseEntity.ok(userDiseases);
    }
}
