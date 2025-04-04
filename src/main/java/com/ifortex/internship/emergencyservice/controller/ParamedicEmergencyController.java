package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.CompleteEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.ParamedicCancelEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.UpdateParamedicLocationRequest;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.service.EmergencyHistoryService;
import com.ifortex.internship.emergencyservice.service.EmergencyService;
import com.ifortex.internship.emergencyservice.service.ParamedicLocationService;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
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
    EmergencyHistoryService historyService;
    ParamedicLocationService paramedicLocationService;

    @Operation(
        summary = "Get assigned emergency",
        description = "Returns the emergency assigned to the authenticated paramedic if it is still ongoing"
    )
    @GetMapping("/assigned")
    public ResponseEntity<ParamedicEmergencyViewDto> getAssignedEmergency(@AuthenticationPrincipal UserDetailsImpl paramedic) {
        UUID paramedicId = paramedic.getAccountId();
        log.info("Paramedic [{}] requested their assigned emergency", paramedicId);
        Optional<ParamedicEmergencyViewDto> emergency = historyService.getCurrentAssignedEmergency(paramedicId);
        return emergency.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(
        summary = "Update current paramedic location",
        description = "Allows paramedic to update their current location"
    )
    @PutMapping("/location")
    public ResponseEntity<Void> updateLocation(
        @Valid @RequestBody UpdateParamedicLocationRequest request,
        @AuthenticationPrincipal UserDetailsImpl paramedic
    ) {
        UUID paramedicId = paramedic.getAccountId();
        log.info("Paramedic [{}] updates location to lat={}, lng={}", paramedicId, request.latitude(), request.longitude());
        paramedicLocationService.updateLocation(request, paramedicId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Cancel assigned emergency",
        description = "Allows paramedic to cancel emergency and provide reason")
    @PutMapping("/assigned/cancel")
    public ResponseEntity<Void> cancelAssignedEmergency(
        @Valid @RequestBody ParamedicCancelEmergencyRequest request,
        @AuthenticationPrincipal UserDetailsImpl paramedic
    ) {
        log.info("Request from paramedic {} to cancel assigned emergency", paramedic.getAccountId());
        emergencyService.cancelAssignedEmergencyByParamedic(request, paramedic.getAccountId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Complete assigned emergency",
        description = "Allows the assigned paramedic to complete the current emergency"
    )
    @PutMapping("/assigned/complete")
    public ResponseEntity<Void> completeAssignedEmergency(
        @Valid @RequestBody CompleteEmergencyRequest request,
        @AuthenticationPrincipal UserDetailsImpl paramedic
    ) {
        log.info("Request from paramedic {} to complete assigned emergency", paramedic.getAccountId());
        emergencyService.completeAssignedEmergency(request, paramedic.getAccountId());
        return ResponseEntity.noContent().build();
    }

}