package com.sparta.logistics.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.logistics.application.port.ShortestPathCache;
import com.sparta.logistics.domain.model.ShortestPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisShortestPathCache implements ShortestPathCache {

    private static final String KEY_PREFIX = "shortest-route:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ShortestPath> get(
            long graphVersion,
            UUID fromHubId,
            UUID toHubId
    ) {
        String key = generateKey(graphVersion, fromHubId, toHubId);

        Object value = redisTemplate.opsForValue().get(key);

        if(value == null){
            return Optional.empty();
        }


        ShortestPath shortestPath =
                objectMapper.convertValue(value, ShortestPath.class);

        return Optional.of(shortestPath);
    }

    @Override
    public void put(
            long graphVersion,
            UUID fromHubId,
            UUID toHubId,
            ShortestPath shortestPath
    ){
        String key = generateKey(graphVersion, fromHubId, toHubId);

        redisTemplate.opsForValue()
                .set(key, shortestPath, TTL);

    }

    private String generateKey(long graphVersion, UUID fromHubId, UUID toHubId){
        return KEY_PREFIX + graphVersion + ":" + fromHubId + ":" + toHubId;
    }
}
