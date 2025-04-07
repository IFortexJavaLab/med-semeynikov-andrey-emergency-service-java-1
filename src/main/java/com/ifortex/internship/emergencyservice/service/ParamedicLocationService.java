package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.accountingapi.AccountingServiceApi;
import com.ifortex.internship.accountingapi.dto.response.AccountDto;
import com.ifortex.internship.accountingapi.exception.CustomFeignException;
import com.ifortex.internship.emergencyservice.dto.request.UpdateParamedicLocationRequest;
import com.ifortex.internship.emergencyservice.model.ParamedicLocation;
import com.ifortex.internship.emergencyservice.repository.ParamedicLocationRepository;
import com.ifortex.internship.emergencyservice.util.AccountDtoMapper;
import com.ifortex.internship.medstarter.exception.custom.InternalServiceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParamedicLocationService {

    AccountDtoMapper accountDtoMapper;
    AccountingServiceApi accountingServiceApi;
    ParamedicLocationRepository locationRepository;

    public void updateLocation(UpdateParamedicLocationRequest request, UUID paramedicId) {
        var accountDto = getAccountDtoFromAccountingService(paramedicId);

        var locationOpt = locationRepository.findById(paramedicId);
        ParamedicLocation location;
        if (locationOpt.isPresent()) {
            location = locationOpt.get();
            location.setLongitude(request.longitude());
            location.setLatitude(request.latitude());
            location.setParamedicFirstName(accountDto.getFirstName());
        } else {
            location = ParamedicLocation.builder()
                .paramedicId(paramedicId)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .paramedicFirstName(accountDto.getFirstName())
                .build();
        }

        locationRepository.save(location);
        log.info("Updated location for paramedic [{}]: lat={}, lng={}",
            paramedicId, request.latitude(), request.longitude());
    }

    private AccountDto getAccountDtoFromAccountingService(UUID paramedicId) {
        AccountDto clientAccount;
        try {
            log.info("Send request to accounting service to get medics profile with account ID: {}", paramedicId);
            var accountDtoResponseEntity = accountingServiceApi.getUserProfileByAuthentication();
            clientAccount = accountDtoMapper.map((Map<String, Object>) accountDtoResponseEntity.getBody());
            log.info("Got user profile with account ID: {} from accounting service", paramedicId);
        } catch (CustomFeignException ex) {
            log.error("Error occurred during call to the auth service");
            throw new InternalServiceException("Something went wrong, try later");
        }
        assert clientAccount != null;
        return clientAccount;
    }
}
