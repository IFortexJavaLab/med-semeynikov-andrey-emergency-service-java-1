package com.ifortex.internship.emergencyservice.unit.service;

import com.ifortex.internship.emergencyservice.model.constant.EmergencyLocationType;
import com.ifortex.internship.emergencyservice.model.emergency.Emergency;
import com.ifortex.internship.emergencyservice.repository.EmergencyRepository;
import com.ifortex.internship.emergencyservice.repository.ParamedicEmergencyLocationRepository;
import com.ifortex.internship.emergencyservice.service.ParamedicEmergencyLocationService;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParamedicEmergencyLocationServiceTest {

    @Mock private EmergencyRepository emergencyRepository;
    @Mock private ParamedicEmergencyLocationRepository locationRepository;
    @InjectMocks private ParamedicEmergencyLocationService service;

    private UUID emergencyId;
    private UUID paramedicId;
    private Emergency emergency;

    @BeforeEach
    void setUp() {
        emergencyId = UUID.randomUUID();
        paramedicId = UUID.randomUUID();
        emergency = new Emergency();
        emergency.setId(emergencyId);
        emergency.setParamedicLocations(new ArrayList<>());
    }

    @Test
    void shouldCreateAndSaveParamedicLocation_successfully() {
        BigDecimal lat = new BigDecimal("59.9311");
        BigDecimal lng = new BigDecimal("30.3609");
        EmergencyLocationType type = EmergencyLocationType.ACCEPTED;

        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(emergency));

        var result = service.createAndSaveParamedicEmergencyLocation(
            lng, lat, paramedicId, emergency, type
        );

        assertNotNull(result);
        assertEquals(paramedicId, result.getParamedicId());
        assertEquals(lat, result.getLatitude());
        assertEquals(lng, result.getLongitude());
        assertEquals(type, result.getLocationType());
        assertEquals(emergency, result.getEmergency());

        verify(locationRepository).save(result);
        assertTrue(emergency.getParamedicLocations().contains(result));
    }

    @Test
    void shouldThrow_whenEmergencyNotFound() {
        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createAndSaveParamedicEmergencyLocation(
            BigDecimal.ONE, BigDecimal.TEN, paramedicId, emergency, EmergencyLocationType.ACCEPTED
        ));

        verify(locationRepository, never()).save(any());
    }
}
