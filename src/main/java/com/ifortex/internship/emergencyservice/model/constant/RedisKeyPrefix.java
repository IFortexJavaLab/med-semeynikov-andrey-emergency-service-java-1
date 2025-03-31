package com.ifortex.internship.emergencyservice.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisKeyPrefix {
    LOCATION_PARAMEDIC("location:paramedic:");

    private final String prefix;
}
