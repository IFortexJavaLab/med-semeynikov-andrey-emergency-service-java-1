package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateCustomDiseaseRequest;
import com.ifortex.internship.emergencyservice.dto.request.EntityIdRequest;
import com.ifortex.internship.emergencyservice.dto.response.UserDiseaseDto;
import com.ifortex.internship.emergencyservice.model.Disease;
import com.ifortex.internship.emergencyservice.model.UserDisease;
import com.ifortex.internship.emergencyservice.repository.UserDiseaseRepository;
import com.ifortex.internship.emergencyservice.service.DiseaseService;
import com.ifortex.internship.emergencyservice.service.UserDiseaseService;
import com.ifortex.internship.emergencyservice.util.UserDiseaseMapper;
import com.ifortex.internship.medstarter.exception.custom.DuplicateResourceException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
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
class UserDiseaseServiceTest {

    @Mock
    private DiseaseService diseaseService;
    @Mock
    private UserDiseaseMapper userDiseaseMapper;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private UserDiseaseRepository userDiseaseRepository;
    @InjectMocks
    private UserDiseaseService userDiseaseService;

    @Test
    void assignDisease_success() {
        UUID diseaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(diseaseId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userDiseaseRepository.existsByUserIdAndDiseaseId(userId, diseaseId)).thenReturn(false);
        Disease disease = new Disease("COVID-19");
        disease.setId(diseaseId);
        when(diseaseService.getDiseaseById(diseaseId)).thenReturn(disease);

        userDiseaseService.assignDisease(request);

        ArgumentCaptor<UserDisease> captor = ArgumentCaptor.forClass(UserDisease.class);
        verify(userDiseaseRepository).save(captor.capture());
        UserDisease saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(disease, saved.getDisease());
    }

    @Test
    void assignDisease_duplicate_shouldThrowException() {
        UUID diseaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(diseaseId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userDiseaseRepository.existsByUserIdAndDiseaseId(userId, diseaseId)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
            () -> userDiseaseService.assignDisease(request));
        assertEquals(String.format("Disease with ID: %s already assigned", diseaseId), ex.getMessage());
    }

    @Test
    void addCustomDisease_success() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        CreateCustomDiseaseRequest request = mock(CreateCustomDiseaseRequest.class);
        String customDiseaseName = "CustomFlu";
        when(request.name()).thenReturn(customDiseaseName);
        when(userDiseaseRepository.existsByUserIdAndCustomDiseaseIgnoreCase(userId, customDiseaseName))
            .thenReturn(false);

        userDiseaseService.addCustomDisease(request);

        ArgumentCaptor<UserDisease> captor = ArgumentCaptor.forClass(UserDisease.class);
        verify(userDiseaseRepository).save(captor.capture());
        UserDisease saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(customDiseaseName, saved.getCustomDisease());
    }

    @Test
    void addCustomDisease_duplicate_shouldThrowException() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        CreateCustomDiseaseRequest request = mock(CreateCustomDiseaseRequest.class);
        String customDiseaseName = "CustomFlu";
        when(request.name()).thenReturn(customDiseaseName);
        when(userDiseaseRepository.existsByUserIdAndCustomDiseaseIgnoreCase(userId, customDiseaseName))
            .thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
            () -> userDiseaseService.addCustomDisease(request));
        assertEquals(String.format("Custom disease: '%s' already assigned", customDiseaseName), ex.getMessage());
    }

    @Test
    void unassignDisease_success() {
        UUID userDiseaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(userDiseaseId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);

        userDiseaseService.unassignDisease(request);
        verify(userDiseaseRepository).deleteByUserIdAndId(userId, userDiseaseId);
    }

    @Test
    void getUserDiseaseProfile_success() {
        UUID userId = UUID.randomUUID();
        UUID fluId = UUID.randomUUID();
        UUID coldId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        UserDisease userDisease1 = new UserDisease(userId, new Disease("Flu"));
        UserDisease userDisease2 = new UserDisease(userId, new Disease("Cold"));
        List<UserDisease> userDiseases = List.of(userDisease1, userDisease2);
        when(userDiseaseRepository.findByUserId(userId)).thenReturn(userDiseases);
        UserDiseaseDto dto1 = new UserDiseaseDto(fluId, "Flu");
        UserDiseaseDto dto2 = new UserDiseaseDto(coldId, "Cold");
        when(userDiseaseMapper.toDto(userDisease1)).thenReturn(dto1);
        when(userDiseaseMapper.toDto(userDisease2)).thenReturn(dto2);

        List<UserDiseaseDto> result = userDiseaseService.getUserDiseaseProfile();
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    @Test
    void getUserDiseaseProfile_empty_shouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userDiseaseRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        List<UserDiseaseDto> result = userDiseaseService.getUserDiseaseProfile();
        assertTrue(result.isEmpty());
    }

    @Test
    void assignDisease_nonExistentDisease_shouldThrowEntityNotFoundException() {
        UUID diseaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EntityIdRequest request = mock(EntityIdRequest.class);
        when(request.asUUID()).thenReturn(diseaseId);
        when(authenticationFacade.getAccountIdFromAuthentication()).thenReturn(userId);
        when(userDiseaseRepository.existsByUserIdAndDiseaseId(userId, diseaseId)).thenReturn(false);
        when(diseaseService.getDiseaseById(diseaseId))
            .thenThrow(new EntityNotFoundException(String.format("Disease with ID: %s not found", diseaseId)));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> userDiseaseService.assignDisease(request));
        assertEquals(String.format("Disease with ID: %s not found", diseaseId), ex.getMessage());
    }

}
