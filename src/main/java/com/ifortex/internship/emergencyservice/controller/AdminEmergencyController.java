package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.request.AdminCompleteEmergencyRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin Emergency Management", description = "Admin operations on emergency lifecycle")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmergencyController {

    EmergencyService emergencyService;

    @Operation(
        summary = "Complete emergency manually",
        description = "Allows admin to manually complete an emergency that has no assigned paramedic"
    )
    @PutMapping("/{emergencyId}/complete")
    public ResponseEntity<Void> finishEmergencyByAdmin(
        @PathVariable UUID emergencyId,
        @Valid @RequestBody AdminCompleteEmergencyRequest request,
        @AuthenticationPrincipal UserDetailsImpl admin
    ) {
        emergencyService.finishEmergencyByAdmin(emergencyId, request, admin.getAccountId());
        return ResponseEntity.noContent().build();
    }
}
