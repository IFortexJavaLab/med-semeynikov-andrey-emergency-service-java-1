package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.dto.response.LocationDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyLocationSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmergencySnapshotMapper {

    @Mapping(target = "initiatorLocation", source = "locations", qualifiedByName = "mapInitiatorLocation")
    @Mapping(target = "symptoms", source = "symptoms", qualifiedByName = "buildSymptomTree")
    @Mapping(target = "userDiseases", source = "diseases")
    @Mapping(target = "userAllergies", source = "allergies")
    ParamedicEmergencyViewDto toParamedicViewDto(EmergencySnapshot snapshot);

    @Named("mapInitiatorLocation")
    static LocationDto mapInitiatorLocation(List<EmergencyLocationSnapshot> locations) {
        return locations.stream()
            .filter(loc -> loc.getLocationType() == EmergencyLocationType.INITIATOR)
            .findFirst()
            .map(loc -> new LocationDto(loc.getLatitude(), loc.getLongitude()))
            .orElse(null);
    }

    //todo refactor
    @Named("buildSymptomTree")
    default List<EmergencySymptomListDto> buildSymptomTree(List<SymptomDto> flatList) {
        if (flatList == null) {
            return List.of();
        }

        Map<UUID, EmergencySymptomListDto> idToNode = new HashMap<>();
        List<EmergencySymptomListDto> roots = new ArrayList<>();

        for (SymptomDto dto : flatList) {
            idToNode.put(dto.id(), new EmergencySymptomListDto(
                dto.id(),
                dto.name(),
                dto.type(),
                dto.advice(),
                dto.animationKey(),
                dto.parentId(),
                new ArrayList<>()
            ));
        }

        for (EmergencySymptomListDto node : idToNode.values()) {
            if (node.parentId() != null && idToNode.containsKey(node.parentId())) {
                idToNode.get(node.parentId()).children().add(node);
            } else {
                roots.add(node);
            }
        }

        return roots;
    }
}