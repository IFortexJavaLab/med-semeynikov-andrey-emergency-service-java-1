package com.ifortex.internship.emergencyservice.websocket;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GeoLocationDto(
    UUID emergencyId,
    UUID paramedicId,
    BigDecimal latitude,
    BigDecimal longitude,
    Instant timestamp
) {
}
