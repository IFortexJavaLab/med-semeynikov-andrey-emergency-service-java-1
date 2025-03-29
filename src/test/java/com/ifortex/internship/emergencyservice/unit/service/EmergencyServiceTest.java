package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.dto.request.CreateEmergencyRequest;
import com.ifortex.internship.emergencyservice.dto.request.UpdateEmergencySymptomsRequest;
import com.ifortex.internship.emergencyservice.dto.response.ParamedicEmergencyViewDto;
import com.ifortex.internship.emergencyservice.dto.response.SymptomDto;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.constant.EmergencyStatus;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.model.emergency.EmergencyLocation;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencyLocationSnapshot;
import com.ifortex.internship.emergencyservice.model.snapshot.EmergencySnapshot;
import com.ifortex.internship.emergencyservice.service.EmergencyService;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock private com.ifortex.internship.emergencyservice.util.SymptomMapper symptomMapper;
    @Mock private com.ifortex.internship.emergencyservice.util.UserAllergyMapper userAllergyMapper;
    @Mock private com.ifortex.internship.emergencyservice.util.UserDiseaseMapper userDiseaseMapper;
    @Mock private com.ifortex.internship.emergencyservice.repository.SymptomRepository symptomRepository;
    @Mock private com.ifortex.internship.emergencyservice.repository.EmergencyRepository emergencyRepository;
    @Mock private com.ifortex.internship.emergencyservice.repository.UserAllergyRepository userAllergyRepository;
    @Mock private com.ifortex.internship.emergencyservice.repository.UserDiseaseRepository userDiseaseRepository;
    @Mock private com.ifortex.internship.emergencyservice.service.ParamedicSearchService paramedicSearchService;
    @Mock private com.ifortex.internship.emergencyservice.util.EmergencyLocationMapper emergencyLocationMapper;
    @Mock private com.ifortex.internship.emergencyservice.util.EmergencySnapshotMapper emergencySnapshotMapper;
    @Mock private com.ifortex.internship.emergencyservice.repository.EmergencyLocationRepository emergencyLocationRepository;
    @Mock private com.ifortex.internship.emergencyservice.repository.EmergencySnapshotRepository emergencySnapshotRepository;

    @InjectMocks
    private EmergencyService emergencyService;

    private final UUID clientId = UUID.randomUUID();
    private UserDetailsImpl client;
    private final String emergencyIdStr = UUID.randomUUID().toString();
    private Emergency emergency;
    private EmergencySnapshot snapshot;

    @BeforeEach
    void setUp() {
        client = mock(UserDetailsImpl.class);
        lenient().when(client.getAccountId()).thenReturn(clientId);
        emergency = new Emergency().setClientId(clientId).setStatus(EmergencyStatus.ONGOING);
        emergency.setId(UUID.fromString(emergencyIdStr));
        emergency.setCreatedAt(Instant.now());
        snapshot = new EmergencySnapshot();
        snapshot.setId(emergencyIdStr);
        snapshot.setCreatedAt(emergency.getCreatedAt());
        snapshot.setStatus(emergency.getStatus());
        snapshot.setClientId(clientId);
        snapshot.setSymptoms(new ArrayList<>());
    }

    @Test
    void createEmergency_success() {
        CreateEmergencyRequest request = mock(CreateEmergencyRequest.class);
        when(request.latitude()).thenReturn(new BigDecimal("55.75"));
        when(request.longitude()).thenReturn(new BigDecimal("37.62"));
        when(request.symptoms()).thenReturn(List.of(UUID.randomUUID()));
        when(emergencyRepository.existsByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(false);
        when(emergencyRepository.save(any(Emergency.class))).thenReturn(emergency);
        EmergencyLocation location = new EmergencyLocation()
            .setEmergency(emergency)
            .setLocationType(EmergencyLocationType.INITIATOR)
            .setLatitude(new BigDecimal("55.75"))
            .setLongitude(new BigDecimal("37.62"));
        when(emergencyLocationRepository.save(any(EmergencyLocation.class))).thenReturn(location);
        when(userAllergyRepository.findByUserId(clientId)).thenReturn(Collections.emptyList());
        when(userAllergyMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(userDiseaseRepository.findByUserId(clientId)).thenReturn(Collections.emptyList());
        when(userDiseaseMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(symptomRepository.findAllWithParentsRecursively(any())).thenReturn(Collections.emptyList());
        when(symptomMapper.toListDtos(any())).thenReturn(Collections.emptyList());
        EmergencyLocationSnapshot locationSnapshot = new EmergencyLocationSnapshot();
        when(emergencyLocationMapper.toSnapshot(any(EmergencyLocation.class))).thenReturn(locationSnapshot);
        when(emergencySnapshotRepository.save(any(EmergencySnapshot.class))).thenReturn(snapshot);
        emergencyService.createEmergency(request, client);
        verify(emergencyRepository, times(1)).save(any(Emergency.class));
        verify(emergencySnapshotRepository, times(1)).save(any(EmergencySnapshot.class));
        verify(paramedicSearchService, times(1)).findParamedicForEmergency(emergency);
        ArgumentCaptor<EmergencyLocation> captor = ArgumentCaptor.forClass(EmergencyLocation.class);
        verify(emergencyLocationMapper, times(1)).toSnapshot(captor.capture());
        EmergencyLocation capturedLocation = captor.getValue();
        assertEquals(new BigDecimal("55.75"), capturedLocation.getLatitude());
        assertEquals(new BigDecimal("37.62"), capturedLocation.getLongitude());
        assertEquals(EmergencyLocationType.INITIATOR, capturedLocation.getLocationType());
    }

    @Test
    void createEmergency_alreadyExists_shouldThrowInvalidRequestException() {
        CreateEmergencyRequest request = mock(CreateEmergencyRequest.class);
        when(emergencyRepository.existsByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(true);
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> emergencyService.createEmergency(request, client));
        assertEquals("You already have an ongoing emergency.", ex.getMessage());
    }

    @Test
    void getSymptomsForCurrentEmergency_noSnapshot_shouldThrowException() {
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> emergencyService.getSymptomsForCurrentEmergency(client));
        assertEquals("No ongoing emergency found", ex.getMessage());
    }

    @Test
    void addSymptomsForCurrentEmergency_success() {
        UUID symptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(symptomId));
        snapshot.setSymptoms(new ArrayList<>());
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(snapshot));
        SymptomDto newSymptom = new SymptomDto(symptomId, "Cough", null, "Rest", "anim2", null);
        when(symptomRepository.findAllWithParentsRecursively(any())).thenReturn(Collections.emptyList());
        when(symptomMapper.toListDtos(any())).thenReturn(List.of(newSymptom));
        emergencyService.addSymptomsForCurrentEmergency(request, client);
        ArgumentCaptor<EmergencySnapshot> snapshotCaptor = ArgumentCaptor.forClass(EmergencySnapshot.class);
        verify(emergencySnapshotRepository).save(snapshotCaptor.capture());
        EmergencySnapshot savedSnapshot = snapshotCaptor.getValue();
        assertTrue(savedSnapshot.getSymptoms().stream().anyMatch(s -> s.id().equals(symptomId)));
    }

    @Test
    void addSymptomsForCurrentEmergency_noSnapshot_shouldThrowException() {
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.empty());
        EntityNotFoundException
            ex =
            assertThrows(EntityNotFoundException.class, () -> emergencyService.addSymptomsForCurrentEmergency(request, client));
        assertEquals("No ongoing emergency found", ex.getMessage());
    }

    @Test
    void deleteSymptomsForCurrentEmergency_success() {
        UUID symptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(symptomId));
        SymptomDto symptomToDelete = new SymptomDto(symptomId, "Headache", null, "Rest", "anim3", null);
        List<SymptomDto> currentSymptoms = new ArrayList<>();
        currentSymptoms.add(symptomToDelete);
        snapshot.setSymptoms(currentSymptoms);
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(snapshot));
        when(symptomRepository.findAllChildrenRecursively(any())).thenReturn(Collections.emptyList());
        when(symptomMapper.toListDtos(any())).thenReturn(List.of(symptomToDelete));
        emergencyService.deleteSymptomsForCurrentEmergency(request, client);
        ArgumentCaptor<EmergencySnapshot> snapshotCaptor = ArgumentCaptor.forClass(EmergencySnapshot.class);
        verify(emergencySnapshotRepository).save(snapshotCaptor.capture());
        EmergencySnapshot savedSnapshot = snapshotCaptor.getValue();
        assertFalse(savedSnapshot.getSymptoms().stream().anyMatch(s -> s.id().equals(symptomId)));
    }

    @Test
    void deleteSymptomsForCurrentEmergency_noSnapshot_shouldThrowException() {
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING)).thenReturn(Optional.empty());
        EntityNotFoundException
            ex =
            assertThrows(EntityNotFoundException.class, () -> emergencyService.deleteSymptomsForCurrentEmergency(request, client));
        assertEquals("No ongoing emergency found", ex.getMessage());
    }

    @Test
    void getAssignedEmergency_success() {
        UUID paramedicId = UUID.randomUUID();
        snapshot.setParamedicId(UUID.randomUUID());
        when(emergencySnapshotRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)).thenReturn(Optional.of(snapshot));
        ParamedicEmergencyViewDto expectedDto = new ParamedicEmergencyViewDto();
        when(emergencySnapshotMapper.toParamedicViewDto(snapshot)).thenReturn(expectedDto);
        ParamedicEmergencyViewDto result = emergencyService.getAssignedEmergency(paramedicId);
        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @Test
    void getAssignedEmergency_notFound_shouldReturnNull() {
        UUID paramedicId = UUID.randomUUID();
        when(emergencySnapshotRepository.findByParamedicIdAndStatus(paramedicId, EmergencyStatus.ONGOING)).thenReturn(Optional.empty());
        ParamedicEmergencyViewDto result = emergencyService.getAssignedEmergency(paramedicId);
        assertNull(result);
    }

    @Test
    void getSymptomsForCurrentEmergency_emptySymptoms_shouldReturnEmptyList() {
        snapshot.setSymptoms(Collections.emptyList());
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        List<?> result = emergencyService.getSymptomsForCurrentEmergency(client);
        assertTrue(result.isEmpty());
    }

    @Test
    void addSymptomsForCurrentEmergency_duplicate_shouldNotAdd() {
        UUID symptomId = UUID.randomUUID();
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(symptomId));
        SymptomDto existingSymptom = new SymptomDto(symptomId, "Cough", null, "Rest", "anim2", null);
        snapshot.setSymptoms(new ArrayList<>(List.of(existingSymptom)));
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        when(symptomRepository.findAllWithParentsRecursively(any())).thenReturn(Collections.emptyList());
        when(symptomMapper.toListDtos(any())).thenReturn(List.of(existingSymptom));
        emergencyService.addSymptomsForCurrentEmergency(request, client);
        ArgumentCaptor<EmergencySnapshot> snapshotCaptor = ArgumentCaptor.forClass(EmergencySnapshot.class);
        verify(emergencySnapshotRepository).save(snapshotCaptor.capture());
        EmergencySnapshot savedSnapshot = snapshotCaptor.getValue();
        assertEquals(1, savedSnapshot.getSymptoms().size());
    }

    @Test
    void deleteSymptomsForCurrentEmergency_emptySymptoms_shouldDoNothing() {
        UpdateEmergencySymptomsRequest request = mock(UpdateEmergencySymptomsRequest.class);
        when(request.symptoms()).thenReturn(List.of(UUID.randomUUID()));
        snapshot.setSymptoms(Collections.emptyList());
        when(emergencySnapshotRepository.findByClientIdAndStatus(clientId, EmergencyStatus.ONGOING))
            .thenReturn(Optional.of(snapshot));
        emergencyService.deleteSymptomsForCurrentEmergency(request, client);
        verify(emergencySnapshotRepository, times(0)).save(any(EmergencySnapshot.class));
    }

}
