package com.ifortex.internship.emergencyservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ParamedicLocationViewDto(
    BigDecimal latitude,
    BigDecimal longitude,
    Instant timestamp
) {
}