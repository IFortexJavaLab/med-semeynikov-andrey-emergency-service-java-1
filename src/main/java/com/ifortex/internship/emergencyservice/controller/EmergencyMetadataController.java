package com.ifortex.internship.emergencyservice.controller;

import com.ifortex.internship.emergencyservice.dto.response.CancellationReasonDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyResolutionDto;
import com.ifortex.internship.emergencyservice.service.EmergencyMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@Tag(name = "Emergency Metadata", description = "Provides resolution and cancellation reason data")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmergencyMetadataController {

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
}
