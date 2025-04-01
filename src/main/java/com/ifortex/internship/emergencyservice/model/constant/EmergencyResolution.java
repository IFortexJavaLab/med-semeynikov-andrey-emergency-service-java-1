package com.ifortex.internship.emergencyservice.model.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EmergencyResolution {

    HOSPITALIZED_WITH_CONSENT("Hospitalized with consent"),
    HOSPITALIZED_UNCONSCIOUS("Hospitalized unconscious"),
    FIRST_AID_NO_HOSPITALIZATION("First aid provided, no hospitalization"),
    NO_HELP_NEEDED("No help needed"),
    FALSE_ALARM("False alarm");

    String description;

}

