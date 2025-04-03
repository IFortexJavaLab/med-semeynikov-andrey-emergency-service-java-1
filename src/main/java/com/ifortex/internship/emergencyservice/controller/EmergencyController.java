package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.response.CancellationReasonDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyListItemDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyResolutionDto;
import com.ifortex.internship.emergencyservice.service.EmergencyHistoryService;
import com.ifortex.internship.emergencyservice.service.EmergencyMetadataService;
import com.ifortex.internship.medstarter.exception.custom.ForbiddenActionException;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@Tag(
    name = "Emergency Management",
    description = "Provides access to emergency metadata (resolutions, cancellation reasons) and emergency history"
)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmergencyController {

    EmergencyHistoryService historyService;
    EmergencyMetadataService emergencyMetadataService;

    @Operation(summary = "Get list of emergency resolution options")
    @GetMapping("/finish-reasons")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARAMEDIC')")
    public List<EmergencyResolutionDto> getEmergencyResolutions() {
        log.info("Fetching emergency resolution list");
        return emergencyMetadataService.getAllEmergencyResolutions();
    }

    @Operation(summary = "Get list of cancellation reasons for assignment")
    @GetMapping("/cancellation-reasons")
    @PreAuthorize("hasRole('PARAMEDIC')")
    public List<CancellationReasonDto> getCancellationReasons() {
        log.info("Fetching cancellation reason list");
        return emergencyMetadataService.getAllCancellationReasons();
    }

    @Operation(
        summary = "Get emergencies list",
        description = "Returns paginated emergencies list depending on the user role")
    @GetMapping
    public ResponseEntity<List<EmergencyListItemDto>> getEmergencyList(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @AuthenticationPrincipal UserDetailsImpl user
    ) {
        log.info("User [{}] requested emergency list. Page = {}, Size = {}",
            user.getAccountId(), page, size);

        return ResponseEntity.ok(historyService.getEmergencyList(user, page, size));
    }

    @Operation(
        summary = "Get emergency details",
        description = "Returns details of emergency based on role")
    @GetMapping("/{emergencyId}")
    public ResponseEntity<?> getEmergencyDetails(
        @PathVariable UUID emergencyId,
        @AuthenticationPrincipal UserDetailsImpl user
    ) {
        log.info("User [{}] requests emergency details for emergency [{}]", user.getAccountId(), emergencyId);

        String role = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElseThrow(() -> new ForbiddenActionException("No role assigned"));

        return switch (role) {
            case "ROLE_ADMIN" -> ResponseEntity.ok(historyService.getEmergencyDetailsForAdmin(emergencyId, user.getAccountId()));
            case "ROLE_PARAMEDIC" -> ResponseEntity.ok(historyService.getEmergencyDetailsForParamedic(emergencyId, user.getAccountId()));
            case "ROLE_CLIENT" -> ResponseEntity.ok(historyService.getEmergencyDetailsForClient(emergencyId, user.getAccountId()));
            default -> {
                log.error("Unauthorized role [{}] attempted to access emergency [{}]", role, emergencyId);
                throw new ForbiddenActionException("Access denied for this role");
            }
        };
    }
}
