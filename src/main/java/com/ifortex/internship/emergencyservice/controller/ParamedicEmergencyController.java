package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.response.EmergencyDto;
import com.ifortex.internship.emergencyservice.service.EmergencyService;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Paramedic Emergency Management", description = "Operations for paramedic emergency handling")
@PreAuthorize("hasRole('PARAMEDIC')")
public class ParamedicEmergencyController {

    EmergencyService emergencyService;

    @Operation(
        summary = "Get assigned emergency",
        description = "Returns the emergency assigned to the authenticated paramedic if it is still ongoing"
    )
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedEmergency(@AuthenticationPrincipal UserDetailsImpl paramedic) {
        UUID paramedicId = paramedic.getAccountId();
        log.info("Paramedic [{}] requested their assigned emergency", paramedicId);
        EmergencyDto emergency = emergencyService.getAssignedEmergency(paramedicId);
        return ResponseEntity.ok(Objects.requireNonNullElse(emergency, "You don't have an ongoing emergency"));
    }
}