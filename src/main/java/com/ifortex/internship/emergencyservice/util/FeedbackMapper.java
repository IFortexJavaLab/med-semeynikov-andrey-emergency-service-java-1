package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.FeedbackDto;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyFeedbackSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    FeedbackDto toDto(EmergencyFeedbackSnapshot entity);
}
