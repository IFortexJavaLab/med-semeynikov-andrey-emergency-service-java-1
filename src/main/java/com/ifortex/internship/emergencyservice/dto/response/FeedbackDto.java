package com.ifortex.internship.emergencyservice.dto.response;

import java.time.Instant;

public record FeedbackDto(
    int grade,
    String comment,
    Instant createdAt
) {
}