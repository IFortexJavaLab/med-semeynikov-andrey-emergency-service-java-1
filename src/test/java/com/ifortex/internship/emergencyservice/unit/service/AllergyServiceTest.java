package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateAllergyDto;
import com.ifortex.internship.emergencyservice.dto.response.AllergyDto;
import com.ifortex.internship.emergencyservice.model.Allergy;
import com.ifortex.internship.emergencyservice.repository.AllergyRepository;
import com.ifortex.internship.emergencyservice.service.AllergyService;
import com.ifortex.internship.emergencyservice.util.AllergyMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllergyServiceTest {

    @Mock
    private AllergyRepository allergyRepository;
    @Mock
    private AllergyMapper allergyMapper;
    @InjectMocks
    private AllergyService allergyService;

    @Test
    void createAllergy_success() {
        CreateAllergyDto dto = mock(CreateAllergyDto.class);
        String name = "Peanuts";
        when(dto.name()).thenReturn(name);
        when(allergyRepository.findByName(name)).thenReturn(Optional.empty());
        Allergy allergy = new Allergy(name);
        UUID allergyId = UUID.randomUUID();
        allergy.setId(allergyId);
        when(allergyRepository.save(any(Allergy.class))).thenReturn(allergy);

        allergyService.createAllergy(dto);

        ArgumentCaptor<Allergy> captor = ArgumentCaptor.forClass(Allergy.class);
        verify(allergyRepository).save(captor.capture());
        assertEquals(name, captor.getValue().getName());
    }

    @Test
    void createAllergy_nullName_shouldThrowInvalidRequestException() {
        CreateAllergyDto dto = mock(CreateAllergyDto.class);
        when(dto.name()).thenReturn(null);
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> allergyService.createAllergy(dto));
        assertEquals("Allergy name must not be null or blank", ex.getMessage());
    }

    @Test
    void createAllergy_blankName_shouldThrowInvalidRequestException() {
        CreateAllergyDto dto = mock(CreateAllergyDto.class);
        when(dto.name()).thenReturn("   ");
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> allergyService.createAllergy(dto));
        assertEquals("Allergy name must not be null or blank", ex.getMessage());
    }

    @Test
    void createAllergy_duplicate_shouldThrowDuplicateResourceException() {
        CreateAllergyDto dto = mock(CreateAllergyDto.class);
        String name = "Peanuts";
        when(dto.name()).thenReturn(name);
        when(allergyRepository.findByName(name)).thenReturn(Optional.of(new Allergy(name)));
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> allergyService.createAllergy(dto));
        assertEquals(String.format("Allergy with name %s already exists.", name), ex.getMessage());
    }

    @Test
    void getAllAllergies_success() {
        int page = 0, size = 5;
        Allergy allergy1 = new Allergy("Peanuts");
        Allergy allergy2 = new Allergy("Shellfish");
        List<Allergy> allergies = List.of(allergy1, allergy2);
        Page<Allergy> allergyPage = new PageImpl<>(allergies);
        when(allergyRepository.findAll(PageRequest.of(page, size))).thenReturn(allergyPage);
        List<AllergyDto> dtoList = List.of(new AllergyDto("1", "Peanuts"), new AllergyDto("2", "Shellfish"));
        when(allergyMapper.toListDtos(allergies)).thenReturn(dtoList);

        List<AllergyDto> result = allergyService.getAllAllergies(page, size);
        assertEquals(dtoList, result);
    }

    @Test
    void updateAllergy_success() {
        String allergyIdStr = UUID.randomUUID().toString();
        AllergyDto dto = new AllergyDto(allergyIdStr, "Dust");
        when(allergyRepository.findByName("Dust")).thenReturn(Optional.empty());
        Allergy existingAllergy = new Allergy("Pollen");
        existingAllergy.setId(UUID.fromString(allergyIdStr));
        when(allergyRepository.findById(UUID.fromString(allergyIdStr))).thenReturn(Optional.of(existingAllergy));
        when(allergyRepository.save(existingAllergy)).thenReturn(existingAllergy);

        allergyService.updateAllergy(dto);

        assertEquals("Dust", existingAllergy.getName());
        verify(allergyRepository).save(existingAllergy);
    }

    @Test
    void updateAllergy_blankName_shouldThrowInvalidRequestException() {
        String allergyIdStr = UUID.randomUUID().toString();
        AllergyDto dto = new AllergyDto(allergyIdStr, "   ");
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> allergyService.updateAllergy(dto));
        assertEquals(String.format("Allergy name must not be null or blank. Allergy ID: %s", allergyIdStr), ex.getMessage());
    }

    @Test
    void updateAllergy_duplicate_shouldThrowDuplicateResourceException() {
        String allergyIdStr = UUID.randomUUID().toString();
        AllergyDto dto = new AllergyDto(allergyIdStr, "Dust");
        when(dto.name()).thenReturn("Dust");
        when(allergyRepository.findByName("Dust")).thenReturn(Optional.of(new Allergy("Dust")));
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> allergyService.updateAllergy(dto));
        assertEquals(String.format("Allergy with name %s already exists.", "Dust"), ex.getMessage());
    }

    @Test
    void updateAllergy_notFound_shouldThrowEntityNotFoundException() {
        String allergyIdStr = UUID.randomUUID().toString();
        AllergyDto dto = new AllergyDto(allergyIdStr, "Dust");
        when(allergyRepository.findByName("Dust")).thenReturn(Optional.empty());
        when(allergyRepository.findById(UUID.fromString(allergyIdStr))).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> allergyService.updateAllergy(dto));
        assertEquals(String.format("Allergy with ID: %s not found", allergyIdStr), ex.getMessage());
    }

    @Test
    void deleteAllergy_success() {
        UUID allergyId = UUID.randomUUID();
        Allergy allergy = new Allergy("Peanuts");
        allergy.setId(allergyId);
        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(allergy));

        allergyService.deleteAllergy(allergyId);

        verify(allergyRepository).delete(allergy);
    }

    @Test
    void deleteAllergy_notFound_shouldThrowEntityNotFoundException() {
        UUID allergyId = UUID.randomUUID();
        when(allergyRepository.findById(allergyId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> allergyService.deleteAllergy(allergyId));
        assertEquals(String.format("Allergy with ID: %s not found", allergyId), ex.getMessage());
    }

    @Test
    void getAllergyById_success() {
        UUID allergyId = UUID.randomUUID();
        Allergy allergy = new Allergy("Peanuts");
        allergy.setId(allergyId);
        when(allergyRepository.findById(allergyId)).thenReturn(Optional.of(allergy));

        Allergy result = allergyService.getAllergyById(allergyId);
        assertEquals(allergy, result);
    }

    @Test
    void getAllergyById_notFound_shouldThrowEntityNotFoundException() {
        UUID allergyId = UUID.randomUUID();
        when(allergyRepository.findById(allergyId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> allergyService.getAllergyById(allergyId));
        assertEquals(String.format("Allergy with ID: %s not found", allergyId), ex.getMessage());
    }
}

