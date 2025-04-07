package com.ifortex.internship.emergencyservice.util;

import com.ifortex.internship.accountingapi.dto.response.AccountDto;
import org.mapstruct.Mapper;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

    default AccountDto map(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return AccountDto.builder()
            .accountId((String) map.get("accountId"))
            .email((String) map.get("email"))
            .phoneNumber((String) map.get("phoneNumber"))
            .firstName((String) map.get("firstName"))
            .lastName((String) map.get("lastName"))
            .isTwoFactorEnabled((Boolean) map.get("twoFactorEnabled"))
            .role((String) map.get("role"))
            .build();
    }
}