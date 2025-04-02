package com.ifortex.internship.emergencyservice.websocket;

import com.ifortex.internship.emergencyservice.service.RedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeoLocationWebSocketController {

    RedisService redisService;

    @MessageMapping("/location/update")
    public void receiveLocation(GeoLocationDto location) {
        redisService.saveLocation(location);

        log.trace("Received location for paramedic: {}, emergency: {}. lat={}, lng={}",
            location.paramedicId(),
            location.emergencyId(),
            location.latitude(),
            location.longitude()
        );
    }
}
