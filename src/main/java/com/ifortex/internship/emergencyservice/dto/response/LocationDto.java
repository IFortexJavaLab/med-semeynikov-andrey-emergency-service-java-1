package com.ifortex.internship.emergencyservice.dto.response;

import java.math.BigDecimal;

public record LocationDto(
    BigDecimal latitude,
    BigDecimal longitude
) {
}
