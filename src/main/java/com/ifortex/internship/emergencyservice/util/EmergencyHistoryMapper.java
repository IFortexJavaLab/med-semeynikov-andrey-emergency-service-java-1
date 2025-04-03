package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.EmergencyListItemDto;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmergencyHistoryMapper {

    EmergencyListItemDto toListItemDto(Emergency entity);
}
