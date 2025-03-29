package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.service.EmergencyService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Client Emergency Management", description = "Operations related to user emergency flow")
@PreAuthorize("@subscriptionSecurity.hasActiveSubscription(authentication)")
public class ClientEmergencyController {

    EmergencyService emergencyService;

    @Operation(summary = "Trigger new emergency", description = "Creates a new emergency and starts paramedic search flow.")
    @PostMapping
    public ResponseEntity<Void> createEmergency(@Valid @RequestBody CreateEmergencyRequest request,
                                                @AuthenticationPrincipal UserDetailsImpl client) {
        log.info("Request to initiate emergency for client [{}]", client.getAccountId());
        emergencyService.createEmergency(request, client);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Get symptoms for current emergency",
        description = "Returns a hierarchical list of symptoms currently associated with the user's ongoing emergency."
    )
    @GetMapping("/current/symptoms")
    public ResponseEntity<List<EmergencySymptomListDto>> getSymptomsForCurrentEmergency(@AuthenticationPrincipal UserDetailsImpl client) {
        log.info("Request to get symptom tree for current emergency by user: [{}]", client.getAccountId());
        List<EmergencySymptomListDto> symptomsTree = emergencyService.getSymptomsForCurrentEmergency(client);
        return ResponseEntity.ok(symptomsTree);
    }

    @Operation(
        summary = "Append symptoms to current emergency",
        description = "Adds the specified symptoms and their respective child symptoms to the current ongoing emergency without replacing existing ones."
    )
    @PatchMapping("/current/symptoms/add")
    public ResponseEntity<Void> addSymptomsForCurrentEmergency(@Valid @RequestBody UpdateEmergencySymptomsRequest request,
                                                               @AuthenticationPrincipal UserDetailsImpl client) {
        log.info("User [{}] requests to update symptoms for current emergency", client.getAccountId());
        emergencyService.addSymptomsForCurrentEmergency(request, client);
        log.info("Symptoms updated successfully for current emergency for user: {}", client.getAccountId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Remove symptoms from current emergency",
        description = "Removes the specified symptoms and all their child symptoms from the current ongoing emergency."
    )
    @PatchMapping("/current/symptoms/delete")
    public ResponseEntity<Void> deleteSymptomsForCurrentEmergency(@Valid @RequestBody UpdateEmergencySymptomsRequest request,
                                                                  @AuthenticationPrincipal UserDetailsImpl client) {
        log.info("User [{}] requests to remove symptoms for current emergency", client.getAccountId());
        emergencyService.deleteSymptomsForCurrentEmergency(request, client);
        log.info("Symptoms removed successfully for current emergency for user: {}", client.getAccountId());
        return ResponseEntity.noContent().build();
    }
}
