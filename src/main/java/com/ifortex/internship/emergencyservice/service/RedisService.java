package com.ifortex.internship.emergencyservice.service;

import com.ifortex.internship.emergencyservice.model.constant.RedisKeyPrefix;
import com.ifortex.internship.emergencyservice.websocket.GeoLocationDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {

    RedisTemplate<String, GeoLocationDto> geoRedisTemplate;

    public void saveLocation(GeoLocationDto location) {
        String key = RedisKeyPrefix.LOCATION.getPrefix() + location.emergencyId() + ":" + location.paramedicId();
        geoRedisTemplate.opsForValue().set(key, location);
    }

    public Optional<GeoLocationDto> getLocation(UUID emergencyId, UUID paramedicId) {
        String key = RedisKeyPrefix.LOCATION.getPrefix() + emergencyId + ":" + paramedicId;
        GeoLocationDto location = geoRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(location);
    }
}
