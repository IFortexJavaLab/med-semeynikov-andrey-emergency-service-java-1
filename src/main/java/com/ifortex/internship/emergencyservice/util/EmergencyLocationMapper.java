package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyLocationSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmergencyLocationMapper {

    @Mapping(source = "emergency.id", target = "emergencyId")
    EmergencyLocationSnapshot toSnapshot(EmergencyLocation location);

    List<EmergencyLocationSnapshot> toList(List<EmergencyLocation> locations);
}
