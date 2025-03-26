package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateEmergencyRequest(@NotNull(message = "Latitude is required")
                                     @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90.0")
                                     @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90.0")
                                     BigDecimal latitude,

                                     @NotNull(message = "Longitude is required")
                                     @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180.0")
                                     @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180.0")
                                     BigDecimal longitude,

                                     @NotNull(message = "Symptom list is required")
                                     @Size(min = 1, message = "At least one symptom must be provided")
                                     List<UUID> symptoms
) {
}