package com.ifortex.internship.emergencyservice.model.snapshot;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmergencyLocationSnapshot {

    Long id;
    UUID emergencyId;
    EmergencyLocationType locationType;
    BigDecimal latitude;
    BigDecimal longitude;
    Instant timestamp;
}
