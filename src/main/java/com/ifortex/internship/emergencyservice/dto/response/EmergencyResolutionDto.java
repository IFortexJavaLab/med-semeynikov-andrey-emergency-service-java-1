package com.ifortex.internship.emergencyservice.dto.response;

public record EmergencyResolutionDto(
    Long id,
    String code,
    String description,
    boolean requiresComment
) {
}
