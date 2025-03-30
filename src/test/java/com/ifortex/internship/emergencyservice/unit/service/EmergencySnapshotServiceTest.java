package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.EmergencySymptomListDto;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.ParamedicEmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.ParamedicEmergencyLocationSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencySnapshotServiceTest {

    @Mock private SymptomService symptomService;
    @Mock private UserAllergyMapper userAllergyMapper;
    @Mock private UserDiseaseMapper userDiseaseMapper;
    @Mock private UserAllergyRepository userAllergyRepository;
    @Mock private UserDiseaseRepository userDiseaseRepository;
    @Mock private EmergencyLocationMapper emergencyLocationMapper;
    @Mock private EmergencySnapshotMapper emergencySnapshotMapper;
    @Mock private EmergencySnapshotRepository emergencySnapshotRepository;
    @InjectMocks private EmergencySnapshotService emergencySnapshotService;

    private Emergency emergency;
    private EmergencySnapshot snapshot;
    private ParamedicEmergencyLocation location;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        emergency = new Emergency().setClientId(clientId).setStatus(EmergencyStatus.ONGOING);
        emergency.setId(UUID.randomUUID());
        emergency.setCreatedAt(java.time.Instant.now());
        snapshot = new EmergencySnapshot();
        snapshot.setId(emergency.getId().toString());
        snapshot.setCreatedAt(emergency.getCreatedAt());
        snapshot.setStatus(emergency.getStatus());
        snapshot.setClientId(clientId);
        snapshot.setLongitude(new BigDecimal("60"));
        snapshot.setLatitude(new BigDecimal("30"));
    }

    @Test
    void createSnapshot_success() {
        when(userAllergyRepository.findByUserId(clientId)).thenReturn(Collections.emptyList());
        when(userAllergyMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(userDiseaseRepository.findByUserId(clientId)).thenReturn(Collections.emptyList());
        when(userDiseaseMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(symptomService.collectParentsSymptomsForEmergency(emergency.getId().toString(), List.of()))
            .thenReturn(Collections.emptyList());
        ParamedicEmergencyLocationSnapshot locSnapshot = new ParamedicEmergencyLocationSnapshot();
        when(emergencyLocationMapper.toSnapshot(location)).thenReturn(locSnapshot);
        when(emergencySnapshotRepository.save(any(EmergencySnapshot.class))).thenReturn(snapshot);

        emergencySnapshotService.createSnapshot(emergency, location, List.of());

        ArgumentCaptor<EmergencySnapshot> captor = ArgumentCaptor.forClass(EmergencySnapshot.class);
        verify(emergencySnapshotRepository).save(captor.capture());
        EmergencySnapshot savedSnapshot = captor.getValue();
        assertEquals(emergency.getId().toString(), savedSnapshot.getId());
        assertEquals(emergency.getCreatedAt(), savedSnapshot.getCreatedAt());
        assertEquals(emergency.getStatus(), savedSnapshot.getStatus());
        assertEquals(clientId, savedSnapshot.getClientId());
        assertNotNull(savedSnapshot.getParamedicLocations());
        assertEquals(1, savedSnapshot.getParamedicLocations().size());
        assertEquals(locSnapshot, savedSnapshot.getParamedicLocations().getFirst());
    }

    @Test
    void addSymptomsForCurrentEmergency_success() {
        UUID newSymptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(newSymptomId));
        snapshot.setSymptoms(new ArrayList<>());
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        SymptomDto newSymptom = new SymptomDto(newSymptomId, "Cough", null, "Rest", "anim", null);
        when(symptomService.collectParentsSymptomsForEmergency(snapshot.getId(), request.symptoms()))
            .thenReturn(List.of(newSymptom));

        emergencySnapshotService.addSymptomsForCurrentEmergency(request, clientId);

        ArgumentCaptor<EmergencySnapshot> captor = ArgumentCaptor.forClass(EmergencySnapshot.class);
        verify(emergencySnapshotRepository).save(captor.capture());
        EmergencySnapshot savedSnapshot = captor.getValue();
        assertTrue(savedSnapshot.getSymptoms().stream().anyMatch(s -> s.id().equals(newSymptomId)));
    }

    @Test
    void addSymptomsForCurrentEmergency_noSnapshot_shouldThrowException() {
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        lenient().when(request.symptoms()).thenReturn(List.of(UUID.randomUUID()));
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> emergencySnapshotService.addSymptomsForCurrentEmergency(request, clientId));
    }

    @Test
    void deleteSymptomsForCurrentEmergency_success() {
        UUID symptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(symptomId));
        List<SymptomDto> currentSymptoms = new ArrayList<>();
        SymptomDto symptomToRemove = new SymptomDto(symptomId, "Headache", null, "Rest", "anim", null);
        currentSymptoms.add(symptomToRemove);
        snapshot.setSymptoms(currentSymptoms);
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        when(symptomService.collectChildSymptomsForEmergency(snapshot.getId(), request.symptoms()))
            .thenReturn(List.of(symptomToRemove));

        emergencySnapshotService.deleteSymptomsForCurrentEmergency(request, clientId);

        ArgumentCaptor<EmergencySnapshot> captor = ArgumentCaptor.forClass(EmergencySnapshot.class);
        verify(emergencySnapshotRepository).save(captor.capture());
        EmergencySnapshot savedSnapshot = captor.getValue();
        assertFalse(savedSnapshot.getSymptoms().stream().anyMatch(s -> s.id().equals(symptomId)));
    }

    @Test
    void deleteSymptomsForCurrentEmergency_emptySymptoms_shouldDoNothing() {
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(UUID.randomUUID()));
        snapshot.setSymptoms(Collections.emptyList());
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));

        emergencySnapshotService.deleteSymptomsForCurrentEmergency(request, clientId);

        verify(emergencySnapshotRepository, never()).save(any(EmergencySnapshot.class));
    }

    @Test
    void getSymptomsForCurrentEmergency_success() {
        List<SymptomDto> symptomList = List.of(
            new SymptomDto(UUID.randomUUID(), "Fever", null, "Drink water", "anim_fever", null)
        );
        snapshot.setSymptoms(symptomList);
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        List<EmergencySymptomListDto> expectedTree = List.of(
            new EmergencySymptomListDto(UUID.randomUUID(), "Fever", null, "Drink water", "anim_fever", null, Collections.emptyList())
        );
        when(emergencySnapshotMapper.buildSymptomTree(symptomList)).thenReturn(expectedTree);

        List<EmergencySymptomListDto> result = emergencySnapshotService.getSymptomsForCurrentEmergency(clientId);
        assertEquals(expectedTree, result);
    }

    @Test
    void getSymptomsForCurrentEmergency_emptySymptoms_shouldReturnEmptyList() {
        snapshot.setSymptoms(Collections.emptyList());
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        List<?> result = emergencySnapshotService.getSymptomsForCurrentEmergency(clientId);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSymptomsForCurrentEmergency_noSnapshot_shouldThrowException() {
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> emergencySnapshotService.getSymptomsForCurrentEmergency(clientId));
    }

    @Test
    void getEmergencySnapshot_success() {
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        EmergencySnapshot result = emergencySnapshotService.getEmergencySnapshot(clientId);
        assertEquals(snapshot, result);
    }

    @Test
    void getEmergencySnapshot_notFound_shouldThrowException() {
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> emergencySnapshotService.getEmergencySnapshot(clientId));
    }

    @Test
    void getAssignedEmergency_success() {
        UUID paramedicId = UUID.randomUUID();
        when(emergencySnapshotRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        ParamedicEmergencyViewDto expectedDto = new ParamedicEmergencyViewDto();
        when(emergencySnapshotMapper.toParamedicViewDto(snapshot)).thenReturn(expectedDto);
        Optional<ParamedicEmergencyViewDto> result = emergencySnapshotService.getAssignedEmergency(paramedicId);
        assertTrue(result.isPresent());
        assertEquals(expectedDto, result.get());
    }

    @Test
    void getAssignedEmergency_notFound_shouldReturnEmptyOptional() {
        UUID paramedicId = UUID.randomUUID();
        when(emergencySnapshotRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.empty());
        Optional<ParamedicEmergencyViewDto> result = emergencySnapshotService.getAssignedEmergency(paramedicId);
        assertTrue(result.isEmpty());
    }
}
