package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCompleteEmergencyRequest(
    @NotNull(message = "Emergency resolution is required") Long emergencyResolutionId,
    @Size(message = "Comment can't be more than 1000 symbols", max = 1000)
    String resolutionExplanation
) {
}
