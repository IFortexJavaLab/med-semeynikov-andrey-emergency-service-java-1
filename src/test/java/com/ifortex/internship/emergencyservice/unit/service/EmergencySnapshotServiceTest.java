package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository;
import com.ifortex.internship.emergencyservice.repository.UserAllergyRepository;
import com.ifortex.internship.emergencyservice.repository.UserDiseaseRepository;
import com.ifortex.internship.emergencyservice.service.EmergencySnapshotService;
import com.ifortex.internship.emergencyservice.service.SymptomService;
import com.ifortex.internship.emergencyservice.util.EmergencyLocationMapper;
import com.ifortex.internship.emergencyservice.util.EmergencySnapshotMapper;
import com.ifortex.internship.emergencyservice.util.UserAllergyMapper;
import com.ifortex.internship.emergencyservice.util.UserDiseaseMapper;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencySnapshotServiceTest {

    @Mock SymptomService symptomService;
    @Mock UserAllergyMapper userAllergyMapper;
    @Mock UserDiseaseMapper userDiseaseMapper;
    @Mock UserAllergyRepository userAllergyRepository;
    @Mock UserDiseaseRepository userDiseaseRepository;
    @Mock EmergencyLocationMapper emergencyLocationMapper;
    @Mock EmergencySnapshotMapper emergencySnapshotMapper;
    @Mock EmergencySnapshotRepository emergencySnapshotRepository;
    @Mock EmergencyRepository emergencyRepository;
    @Mock AuthenticationFacade authenticationFacade;

    @InjectMocks EmergencySnapshotService emergencySnapshotService;

    Emergency emergency;
    EmergencySnapshot snapshot;
    UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        emergency = new Emergency()
            .setClientId(clientId)
            .setStatus(EmergencyStatus.ONGOING)
            .setLatitude(new BigDecimal("60"))
            .setLongitude(new BigDecimal("30"));
        emergency.setId(UUID.randomUUID());
        emergency.setCreatedAt(Instant.now());

        snapshot = new EmergencySnapshot();
        snapshot.setId(emergency.getId().toString());
        snapshot.setCreatedAt(emergency.getCreatedAt());
        snapshot.setStatus(emergency.getStatus());
        snapshot.setClientFirstName("Aboba");
        snapshot.setLatitude(emergency.getLatitude());
        snapshot.setLongitude(emergency.getLongitude());
    }

    @Test
    void createSnapshot_success() {
        when(authenticationFacade.getUserFirstNameFromAuthentication()).thenReturn("Aboba");
        when(userAllergyRepository.findByUserId(clientId)).thenReturn(Collections.emptyList());
        when(userAllergyMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(userDiseaseRepository.findByUserId(clientId)).thenReturn(Collections.emptyList());
        when(userDiseaseMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(symptomService.collectParentsSymptomsForEmergency(any(), any())).thenReturn(Collections.emptyList());
        lenient().when(emergencyLocationMapper.toSnapshot(any())).thenReturn(new ParamedicEmergencyLocationSnapshot());
        when(emergencySnapshotRepository.save(any())).thenReturn(snapshot);

        emergencySnapshotService.createSnapshot(emergency, List.of());

        verify(emergencySnapshotRepository).save(any(EmergencySnapshot.class));
    }

    @Test
    void addSymptomsForCurrentEmergency_success() {
        UUID symptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(symptomId));
        snapshot.setSymptoms(new ArrayList<>());

        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.of(snapshot));
        when(symptomService.collectParentsSymptomsForEmergency(any(), any()))
            .thenReturn(List.of(new SymptomDto(symptomId, "Cough", null, "", "", null)));

        emergencySnapshotService.addSymptomsForCurrentEmergency(request, clientId);

        assertEquals(1, snapshot.getSymptoms().size());
        verify(emergencySnapshotRepository).save(snapshot);
    }

    @Test
    void deleteSymptomsForCurrentEmergency_success() {
        UUID symptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(symptomId));

        SymptomDto symptom = new SymptomDto(symptomId, "Headache", null, "", "", null);
        snapshot.setSymptoms(new ArrayList<>(List.of(symptom)));

        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.of(snapshot));
        when(symptomService.collectChildSymptomsForEmergency(snapshot.getId(), request.symptoms()))
            .thenReturn(List.of(symptom));

        emergencySnapshotService.deleteSymptomsForCurrentEmergency(request, clientId);

        assertTrue(snapshot.getSymptoms().isEmpty());
        verify(emergencySnapshotRepository).save(snapshot);
    }

    @Test
    void deleteSymptomsForCurrentEmergency_emptySymptoms_shouldDoNothing() {
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(UUID.randomUUID()));
        snapshot.setSymptoms(Collections.emptyList());

        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.of(snapshot));

        emergencySnapshotService.deleteSymptomsForCurrentEmergency(request, clientId);

        verify(emergencySnapshotRepository, never()).save(any());
    }

    @Test
    void getSymptomsForCurrentEmergency_success() {
        List<SymptomDto> symptoms = List.of(new SymptomDto(UUID.randomUUID(), "Fever", null, "", "", null));
        snapshot.setSymptoms(symptoms);

        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.of(snapshot));
        when(emergencySnapshotMapper.buildSymptomTree(symptoms)).thenReturn(List.of());

        List<EmergencySymptomListDto> result = emergencySnapshotService.getSymptomsForCurrentEmergency(clientId);
        assertNotNull(result);
    }

    @Test
    void getSymptomsForCurrentEmergency_noSnapshot_shouldThrow() {
        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> emergencySnapshotService.getSymptomsForCurrentEmergency(clientId));
    }

    @Test
    void getEmergencySnapshotByClientIdAndStatus_success() {
        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.of(snapshot));

        EmergencySnapshot result = emergencySnapshotService.getEmergencySnapshotByClientIdAndStatus(clientId, EmergencyStatus.ONGOING);
        assertEquals(snapshot, result);
    }

    @Test
    void getEmergencySnapshotByClientIdAndStatus_notFound_shouldThrow() {
        when(emergencyRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            emergencySnapshotService.getEmergencySnapshotByClientIdAndStatus(clientId, EmergencyStatus.ONGOING));
    }

    @Test
    void getAssignedEmergency_success() {
        UUID paramedicId = UUID.randomUUID();

        when(emergencyRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(emergency));
        when(emergencySnapshotRepository.findById(emergency.getId().toString())).thenReturn(Optional.of(snapshot));
        when(emergencySnapshotMapper.toParamedicViewDto(snapshot)).thenReturn(new ParamedicEmergencyViewDto());

        Optional<ParamedicEmergencyViewDto> result = emergencySnapshotService.getAssignedEmergency(paramedicId);

        assertTrue(result.isPresent());
    }

    @Test
    void getAssignedEmergency_notFound_shouldThrow() {
        UUID paramedicId = UUID.randomUUID();

        when(emergencyRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            emergencySnapshotService.getAssignedEmergency(paramedicId));
    }
}
