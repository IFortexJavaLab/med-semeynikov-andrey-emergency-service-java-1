package com.ifortex.internship.emergencyservice.model.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CancellationReason {

    LOCATION_NOT_REACHABLE("Location not reachable"),
    MUNICIPAL_SERVICES_PROVIDED("Municipal emergency services provided"),
    ACCIDENT_ON_WAY("Accident on a way"),
    BLOCKED_BY_OTHERS("The way to an emergency is blocked by actions of other persons");

    String description;

}