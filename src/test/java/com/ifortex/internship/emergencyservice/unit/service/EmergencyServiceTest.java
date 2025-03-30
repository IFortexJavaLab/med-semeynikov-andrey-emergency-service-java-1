package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import com.ifortex.internship.emergencyservice.repository.EmergencyLocationRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.service.EmergencyService;
import com.ifortex.internship.emergencyservice.service.EmergencySnapshotService;
import com.ifortex.internship.emergencyservice.service.ParamedicSearchService;
import com.ifortex.internship.medstarter.exception.custom.InvalidRequestException;
import com.ifortex.internship.medstarter.security.model.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock private EmergencyRepository emergencyRepository;
    @Mock private ParamedicSearchService paramedicSearchService;
    @Mock private EmergencySnapshotService emergencySnapshotService;
    @Mock private EmergencyLocationRepository emergencyLocationRepository;
    @InjectMocks private EmergencyService emergencyService;

    @Mock private UserDetailsImpl client;
    private UUID clientId;
    private Emergency emergency;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        when(client.getAccountId()).thenReturn(clientId);
        emergency = new Emergency().setClientId(clientId).setStatus(EmergencyStatus.ONGOING);
        emergency.setId(UUID.randomUUID());
        emergency.setCreatedAt(Instant.now());
    }

    @Test
    void createEmergency_success() {
        CreateEmergencyRequest request = new CreateEmergencyRequest(
            new BigDecimal("55.75"),
            new BigDecimal("37.62"),
            List.of(UUID.randomUUID())
        );
        when(emergencyRepository.existsByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(false);
        when(emergencyRepository.save(any(Emergency.class))).thenReturn(emergency);

        doNothing().when(emergencySnapshotService).createSnapshot(any(Emergency.class), any(ParamedicEmergencyLocation.class), anyList());
        doNothing().when(paramedicSearchService).findParamedicForEmergency(any(Emergency.class));

        emergencyService.createEmergency(request, client);

        verify(emergencyRepository, times(1)).save(any(Emergency.class));
        verify(emergencyLocationRepository, times(1)).save(any(ParamedicEmergencyLocation.class));

        ArgumentCaptor<Emergency> emergencyCaptor = ArgumentCaptor.forClass(Emergency.class);
        ArgumentCaptor<ParamedicEmergencyLocation> locationCaptor = ArgumentCaptor.forClass(ParamedicEmergencyLocation.class);
        ArgumentCaptor<List> symptomsCaptor = ArgumentCaptor.forClass(List.class);
        verify(emergencySnapshotService, times(1)).createSnapshot(
            emergencyCaptor.capture(),
            locationCaptor.capture(),
            symptomsCaptor.capture()
        );
        assertEquals(emergency, emergencyCaptor.getValue());
        assertEquals(request.symptoms(), symptomsCaptor.getValue());
        verify(paramedicSearchService, times(1)).findParamedicForEmergency(emergency);
    }

    @Test
    void createEmergency_alreadyExists_shouldThrowInvalidRequestException() {
        CreateEmergencyRequest request = new CreateEmergencyRequest(new BigDecimal("55.75"), new BigDecimal("37.62"), List.of(UUID.randomUUID()));
        when(emergencyRepository.existsByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(true);
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> emergencyService.createEmergency(request, client));
        assertEquals("You already have an ongoing emergency.", ex.getMessage());
        verify(emergencyRepository, never()).save(any(Emergency.class));
        verify(emergencySnapshotService, never()).createSnapshot(any(), any(), any());
        verify(paramedicSearchService, never()).findParamedicForEmergency(any());
    }
}

