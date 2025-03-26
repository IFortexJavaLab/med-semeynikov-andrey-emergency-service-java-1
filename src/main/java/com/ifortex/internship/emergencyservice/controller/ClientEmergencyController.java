package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.service.ClientEmergencyService;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Emergency Management", description = "Operations related to user emergency flow")
@PreAuthorize("@subscriptionSecurity.hasActiveSubscription(authentication)")
public class ClientEmergencyController {

    ClientEmergencyService clientEmergencyService;

    @Operation(summary = "Trigger new emergency", description = "Creates a new emergency and starts paramedic search flow.")
    @PostMapping
    public ResponseEntity<Void> createEmergency(@Valid @RequestBody CreateEmergencyRequest request,
                                                @AuthenticationPrincipal UserDetailsImpl client) {
        log.info("Request to initiate emergency for user: {}", client.getAccountId());
        clientEmergencyService.createEmergency(request, client);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
