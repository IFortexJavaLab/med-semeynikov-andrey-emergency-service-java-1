package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateCustomAllergyRequest;
import com.ifortex.internship.emergencyservice.dto.request.EntityIdRequest;
import com.ifortex.internship.emergencyservice.dto.response.UserAllergyDto;
import com.ifortex.internship.emergencyservice.model.Allergy;
import com.ifortex.internship.emergencyservice.model.UserAllergy;
import com.ifortex.internship.emergencyservice.repository.UserAllergyRepository;
import com.ifortex.internship.emergencyservice.service.AllergyService;
import com.ifortex.internship.emergencyservice.service.UserAllergyService;
import com.ifortex.internship.emergencyservice.util.UserAllergyMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAllergyServiceTest {

    @Mock
    private AllergyService allergyService;
    @Mock
    private UserAllergyMapper userAllergyMapper;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private UserAllergyRepository userAllergyRepository;
    @InjectMocks
    private UserAllergyService userAllergyService;

    @Test
    void assignAllergy_success() {
        UUID allergyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(allergyId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userAllergyRepository.existsByUserIdAndAllergyId(userId, allergyId)).thenReturn(false);

        Allergy allergy = new Allergy("Pollen");
        allergy.setId(allergyId);
        when(allergyService.getAllergyById(allergyId)).thenReturn(allergy);

        userAllergyService.assignAllergy(request);

        ArgumentCaptor<UserAllergy> captor = ArgumentCaptor.forClass(UserAllergy.class);
        verify(userAllergyRepository).save(captor.capture());
        UserAllergy saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(allergy, saved.getAllergy());
    }

    @Test
    void assignAllergy_duplicate_shouldThrowException() {
        UUID allergyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(allergyId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userAllergyRepository.existsByUserIdAndAllergyId(userId, allergyId)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
            () -> userAllergyService.assignAllergy(request));
        assertEquals(String.format("Allergy with ID: %s already assigned", allergyId), ex.getMessage());
    }

    @Test
    void addCustomAllergy_success() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        CreateCustomAllergyRequest request = mock(CreateCustomAllergyRequest.class);
        String customAllergy = "CustomDust";
        when(request.name()).thenReturn(customAllergy);
        when(userAllergyRepository.existsByUserIdAndCustomAllergyIgnoreCase(userId, customAllergy))
            .thenReturn(false);

        userAllergyService.addCustomAllergy(request);

        ArgumentCaptor<UserAllergy> captor = ArgumentCaptor.forClass(UserAllergy.class);
        verify(userAllergyRepository).save(captor.capture());
        UserAllergy saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(customAllergy, saved.getCustomAllergy());
    }

    @Test
    void addCustomAllergy_duplicate_shouldThrowException() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        CreateCustomAllergyRequest request = mock(CreateCustomAllergyRequest.class);
        String customAllergy = "CustomDust";
        when(request.name()).thenReturn(customAllergy);
        when(userAllergyRepository.existsByUserIdAndCustomAllergyIgnoreCase(userId, customAllergy))
            .thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
            () -> userAllergyService.addCustomAllergy(request));
        assertEquals(String.format("Custom allergy: '%s' already assigned", customAllergy), ex.getMessage());
    }

    @Test
    void unassignAllergy_success() {
        UUID userAllergyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(userAllergyId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);

        userAllergyService.unassignAllergy(request);

        verify(userAllergyRepository).deleteByUserIdAndId(userId, userAllergyId);
    }

    @Test
    void getUserAllergyProfile_success() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        UserAllergy userAllergy1 = new UserAllergy(userId, new Allergy("Peanuts"));
        UserAllergy userAllergy2 = new UserAllergy(userId, new Allergy("Shellfish"));
        List<UserAllergy> allergyList = List.of(userAllergy1, userAllergy2);
        when(userAllergyRepository.findByUserId(userId)).thenReturn(allergyList);

        UserAllergyDto dto1 = new UserAllergyDto(UUID.randomUUID(), "Peanuts");
        UserAllergyDto dto2 = new UserAllergyDto(UUID.randomUUID(), "Shellfish");
        when(userAllergyMapper.toDto(userAllergy1)).thenReturn(dto1);
        when(userAllergyMapper.toDto(userAllergy2)).thenReturn(dto2);

        List<UserAllergyDto> result = userAllergyService.getUserAllergyProfile();
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    @Test
    void getUserAllergyProfile_empty_shouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userAllergyRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<UserAllergyDto> result = userAllergyService.getUserAllergyProfile();
        assertTrue(result.isEmpty());
    }
}
