package com.ifortex.internship.emergencyservice.unit.util;

import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.model.Symptom;
import com.ifortex.internship.emergencyservice.model.constant.SymptomType;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencySymptom;
import com.ifortex.internship.emergencyservice.util.EmergencySymptomMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmergencySymptomMapperTest {

    private final EmergencySymptomMapper mapper = new EmergencySymptomMapper();

    @Test
    void toTreeDto_shouldBuildCorrectTree() {
        // Симптомы: root -> child1, child2
        UUID rootId = UUID.randomUUID();
        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();

        Symptom root = new Symptom();
        root.setId(rootId);
        root.setName("Pain");
        root.setType(SymptomType.CHECKBOX);

        Symptom child1 = new Symptom();
        child1.setId(child1Id);
        child1.setName("Chest pain");
        child1.setType(SymptomType.CHECKBOX);
        child1.setParent(root);

        Symptom child2 = new Symptom();
        child2.setId(child2Id);
        child2.setName("Headache");
        child2.setType(SymptomType.CHECKBOX);
        child2.setParent(root);

        EmergencySymptom es1 = new EmergencySymptom();
        es1.setSymptom(root);
        EmergencySymptom es2 = new EmergencySymptom();
        es2.setSymptom(child1);
        EmergencySymptom es3 = new EmergencySymptom();
        es3.setSymptom(child2);

        List<EmergencySymptomListDto> tree = mapper.toTreeDto(List.of(es1, es2, es3));

        assertThat(tree).hasSize(1);
        EmergencySymptomListDto rootDto = tree.get(0);
        assertThat(rootDto.id()).isEqualTo(rootId);
        assertThat(rootDto.children()).hasSize(2);

        List<UUID> childIds = rootDto.children().stream().map(EmergencySymptomListDto::id).toList();
        assertThat(childIds).containsExactlyInAnyOrder(child1Id, child2Id);
    }

    @Test
    void toTreeDto_shouldReturnEmptyList_whenInputIsEmpty() {
        List<EmergencySymptomListDto> tree = mapper.toTreeDto(List.of());
        assertThat(tree).isEmpty();
    }
}
