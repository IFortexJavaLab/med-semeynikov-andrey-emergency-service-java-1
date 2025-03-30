package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmergencyLocationMapper {

    @Mapping(source = "emergency.id", target = "emergencyId")
    ParamedicEmergencyLocationSnapshot toSnapshot(ParamedicEmergencyLocation location);

    List<ParamedicEmergencyLocationSnapshot> toList(List<ParamedicEmergencyLocation> locations);
}
