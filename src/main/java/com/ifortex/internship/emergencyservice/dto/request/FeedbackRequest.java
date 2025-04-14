package com.ifortex.internship.emergencyservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
    @Min(message = "Grade must be from 1 to 5", value = 1)
    @Max(message = "Grade must be from 1 to 5", value = 5)
    int grade,
    @Size(message = "Comment can't be more than 1000 symbols", max = 1000)
    String comment) {
}
