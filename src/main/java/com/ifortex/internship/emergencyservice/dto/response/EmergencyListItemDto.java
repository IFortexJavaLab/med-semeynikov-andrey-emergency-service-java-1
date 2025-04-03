package com.ifortex.internship.emergencyservice.dto.response;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;

import java.time.Instant;
import java.util.UUID;

public record EmergencyListItemDto(
    UUID id,
    EmergencyStatus status,
    Instant createdAt
) {
}
