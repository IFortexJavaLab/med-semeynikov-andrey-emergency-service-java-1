package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.ParamedicLocationViewDto;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParamedicLocationMapper {

    ParamedicLocationViewDto toViewDto(ParamedicEmergencyLocationSnapshot paramedicEmergencyLocation);
}
