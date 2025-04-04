package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.model.emergency.EmergencyAssignment;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyAssignmentSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmergencyAssignmentMapper {

    @Mapping(source = "emergency.id", target = "emergencyId")
    @Mapping(source = "cancellationReason.description", target = "cancellationReason")
    EmergencyAssignmentSnapshot toSnapshot(EmergencyAssignment entity);
}
