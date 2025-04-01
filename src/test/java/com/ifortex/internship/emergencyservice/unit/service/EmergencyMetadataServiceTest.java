package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.response.CancellationReasonDto;
import com.ifortex.internship.emergencyservice.dto.response.EmergencyResolutionDto;
import com.ifortex.internship.emergencyservice.model.emergency.CancellationReasonEntity;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyResolutionEntity;
import com.ifortex.internship.emergencyservice.repository.CancellationReasonRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyResolutionRepository;
import com.ifortex.internship.emergencyservice.service.EmergencyMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyMetadataServiceTest {

    @Mock
    EmergencyResolutionRepository emergencyResolutionRepository;

    @Mock
    CancellationReasonRepository cancellationReasonRepository;

    @InjectMocks
    EmergencyMetadataService emergencyMetadataService;

    EmergencyResolutionEntity resolution1;
    EmergencyResolutionEntity resolution2;
    CancellationReasonEntity reason1;
    CancellationReasonEntity reason2;

    @BeforeEach
    void setUp() {
        resolution1 = EmergencyResolutionEntity.builder()
            .id(1L)
            .code("HOSPITALIZED_WITH_CONSENT")
            .description("Hospitalized with consent")
            .requiresComment(false)
            .build();

        resolution2 = EmergencyResolutionEntity.builder()
            .id(2L)
            .code("FALSE_ALARM")
            .description("False alarm")
            .requiresComment(true)
            .build();

        reason1 = CancellationReasonEntity.builder()
            .id(1L)
            .code("BLOCKED_BY_OTHERS")
            .description("Blocked by others")
            .requiresComment(true)
            .build();

        reason2 = CancellationReasonEntity.builder()
            .id(2L)
            .code("ACCIDENT_ON_WAY")
            .description("Accident on a way")
            .requiresComment(false)
            .build();
    }

    @Test
    void getAllEmergencyResolutions_shouldReturnList() {
        when(emergencyResolutionRepository.findAll()).thenReturn(List.of(resolution1, resolution2));

        List<EmergencyResolutionDto> result = emergencyMetadataService.getAllEmergencyResolutions();

        assertEquals(2, result.size());
        assertEquals("HOSPITALIZED_WITH_CONSENT", result.get(0).code());
        assertEquals("False alarm", result.get(1).description());

        verify(emergencyResolutionRepository, times(1)).findAll();
    }

    @Test
    void getAllCancellationReasons_shouldReturnList() {
        when(cancellationReasonRepository.findAll()).thenReturn(List.of(reason1, reason2));

        List<CancellationReasonDto> result = emergencyMetadataService.getAllCancellationReasons();

        assertEquals(2, result.size());
        assertTrue(result.get(0).requiresComment());
        assertEquals("ACCIDENT_ON_WAY", result.get(1).code());

        verify(cancellationReasonRepository, times(1)).findAll();
    }

    @Test
    void getAllEmergencyResolutions_empty_shouldReturnEmptyList() {
        when(emergencyResolutionRepository.findAll()).thenReturn(Collections.emptyList());

        List<EmergencyResolutionDto> result = emergencyMetadataService.getAllEmergencyResolutions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(emergencyResolutionRepository, times(1)).findAll();
    }

    @Test
    void getAllCancellationReasons_empty_shouldReturnEmptyList() {
        when(cancellationReasonRepository.findAll()).thenReturn(Collections.emptyList());

        List<CancellationReasonDto> result = emergencyMetadataService.getAllCancellationReasons();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cancellationReasonRepository, times(1)).findAll();
    }

    @Test
    void getAllEmergencyResolutions_repositoryThrows_shouldPropagateException() {
        when(emergencyResolutionRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        Exception exception = assertThrows(RuntimeException.class,
            () -> emergencyMetadataService.getAllEmergencyResolutions());
        assertEquals("DB error", exception.getMessage());
    }

    @Test
    void getAllCancellationReasons_repositoryThrows_shouldPropagateException() {
        when(cancellationReasonRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        Exception exception = assertThrows(RuntimeException.class,
            () -> emergencyMetadataService.getAllCancellationReasons());
        assertEquals("DB error", exception.getMessage());
    }
}
