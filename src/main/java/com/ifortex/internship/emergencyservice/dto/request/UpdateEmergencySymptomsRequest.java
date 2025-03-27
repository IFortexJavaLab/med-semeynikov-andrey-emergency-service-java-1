package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateEmergencySymptomsRequest(
    @NotEmpty(message = "Symptoms list must not be empty")
    List<@NotNull UUID> symptoms
) {
}
