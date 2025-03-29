package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateDiseaseDto;
import com.ifortex.internship.emergencyservice.dto.response.DiseaseDto;
import com.ifortex.internship.emergencyservice.model.Disease;
import com.ifortex.internship.emergencyservice.repository.DiseaseRepository;
import com.ifortex.internship.emergencyservice.service.DiseaseService;
import com.ifortex.internship.emergencyservice.util.DiseaseMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiseaseServiceTest {

    @Mock
    private DiseaseRepository diseaseRepository;
    @Mock
    private DiseaseMapper diseaseMapper;
    @InjectMocks
    private DiseaseService diseaseService;

    @Test
    void createDisease_success() {
        CreateDiseaseDto createDiseaseDto = mock(CreateDiseaseDto.class);
        String diseaseName = "Flu";
        when(createDiseaseDto.name()).thenReturn(diseaseName);
        when(diseaseRepository.findByName(diseaseName)).thenReturn(Optional.empty());
        Disease disease = new Disease(diseaseName);
        disease.setId(UUID.randomUUID());
        when(diseaseRepository.save(any(Disease.class))).thenReturn(disease);
        diseaseService.createDisease(createDiseaseDto);
        ArgumentCaptor<Disease> captor = ArgumentCaptor.forClass(Disease.class);
        verify(diseaseRepository).save(captor.capture());
        assertEquals(diseaseName, captor.getValue().getName());
    }

    @Test
    void createDisease_duplicate_shouldThrowException() {
        CreateDiseaseDto createDiseaseDto = mock(CreateDiseaseDto.class);
        String diseaseName = "Flu";
        when(createDiseaseDto.name()).thenReturn(diseaseName);
        when(diseaseRepository.findByName(diseaseName)).thenReturn(Optional.of(new Disease(diseaseName)));
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> diseaseService.createDisease(createDiseaseDto));
        assertEquals(String.format("Disease with name %s already exists.", diseaseName), ex.getMessage());
    }

    @Test
    void getAllDiseases_success() {
        int page = 0, size = 10;
        UUID fluId = UUID.randomUUID();
        UUID coldId = UUID.randomUUID();
        Disease flu = new Disease("Flu");
        flu.setId(fluId);
        Disease cold = new Disease("Cold");
        cold.setId(coldId);
        List<Disease> diseaseList = List.of(flu, cold);
        Page<Disease> pageResult = new PageImpl<>(diseaseList);
        when(diseaseRepository.findAll(PageRequest.of(page, size))).thenReturn(pageResult);
        List<DiseaseDto> dtoList = List.of(
            new DiseaseDto(fluId.toString(), "Flu"),
            new DiseaseDto(coldId.toString(), "Cold")
        );
        when(diseaseMapper.toListDtos(diseaseList)).thenReturn(dtoList);
        List<DiseaseDto> result = diseaseService.getAllDiseases(page, size);
        assertEquals(dtoList, result);
    }

    @Test
    void getAllDiseases_emptyPage_shouldReturnEmptyList() {
        int page = 0, size = 10;
        Page<Disease> emptyPage = Page.empty();
        when(diseaseRepository.findAll(PageRequest.of(page, size))).thenReturn(emptyPage);
        when(diseaseMapper.toListDtos(Collections.emptyList())).thenReturn(Collections.emptyList());
        List<DiseaseDto> result = diseaseService.getAllDiseases(page, size);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateDisease_success() {
        String idStr = UUID.randomUUID().toString();
        DiseaseDto diseaseDto = new DiseaseDto(idStr, "NewName");
        when(diseaseRepository.findByName("NewName")).thenReturn(Optional.empty());
        Disease existingDisease = new Disease("OldName");
        existingDisease.setId(UUID.fromString(idStr));
        when(diseaseRepository.findById(UUID.fromString(idStr))).thenReturn(Optional.of(existingDisease));
        when(diseaseRepository.save(existingDisease)).thenReturn(existingDisease);
        diseaseService.updateDisease(diseaseDto);
        assertEquals("NewName", existingDisease.getName());
        verify(diseaseRepository).save(existingDisease);
    }

    @Test
    void updateDisease_duplicate_shouldThrowException() {
        String idStr = UUID.randomUUID().toString();
        DiseaseDto diseaseDto = new DiseaseDto(idStr, "NewName");
        when(diseaseRepository.findByName("NewName")).thenReturn(Optional.of(new Disease("NewName")));
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> diseaseService.updateDisease(diseaseDto));
        assertEquals(String.format("Disease with name %s already exists.", "NewName"), ex.getMessage());
    }

    @Test
    void updateDisease_diseaseNotFound_shouldThrowException() {
        String idStr = UUID.randomUUID().toString();
        DiseaseDto diseaseDto = new DiseaseDto(idStr, "NonExistent");
        when(diseaseRepository.findByName("NonExistent")).thenReturn(Optional.empty());
        when(diseaseRepository.findById(UUID.fromString(idStr))).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> diseaseService.updateDisease(diseaseDto));
        assertEquals(String.format("Disease with ID: %s not found", idStr), ex.getMessage());
    }

    @Test
    void deleteDisease_success() {
        UUID diseaseId = UUID.randomUUID();
        Disease disease = new Disease("Flu");
        disease.setId(diseaseId);
        when(diseaseRepository.findById(diseaseId)).thenReturn(Optional.of(disease));
        diseaseService.deleteDisease(diseaseId);
        verify(diseaseRepository).delete(disease);
    }

    @Test
    void deleteDisease_diseaseNotFound_shouldThrowException() {
        UUID diseaseId = UUID.randomUUID();
        when(diseaseRepository.findById(diseaseId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> diseaseService.deleteDisease(diseaseId));
        assertEquals(String.format("Disease with ID: %s not found", diseaseId), ex.getMessage());
    }

    @Test
    void getDiseaseById_success() {
        UUID diseaseId = UUID.randomUUID();
        Disease disease = new Disease("Flu");
        disease.setId(diseaseId);
        when(diseaseRepository.findById(diseaseId)).thenReturn(Optional.of(disease));
        Disease result = diseaseService.getDiseaseById(diseaseId);
        assertEquals(disease, result);
    }

    @Test
    void getDiseaseById_notFound_shouldThrowException() {
        UUID diseaseId = UUID.randomUUID();
        when(diseaseRepository.findById(diseaseId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> diseaseService.getDiseaseById(diseaseId));
        assertEquals(String.format("Disease with ID: %s not found", diseaseId), ex.getMessage());
    }
}
