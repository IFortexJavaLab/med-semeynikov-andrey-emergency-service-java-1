package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.Size;

public record CancelEmergencyRequest(
    @Size(message = "Comment can't be more than 1000 symbols", max = 1000)
    String cancellationComment
) {
}