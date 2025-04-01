package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminCompleteEmergencyRequest(
    @NotNull(message = "Emergency resolution is required") Long emergencyResolutionId,
    String resolutionExplanation
) {
}
