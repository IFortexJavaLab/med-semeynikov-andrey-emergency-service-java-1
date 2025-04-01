package com.ifortex.internship.emergencyservice.dto.response;

public record CancellationReasonDto(
    Long id,
    String code,
    String description,
    boolean requiresComment
) {
}
