package com.example.gorzdrav_spb_bot.handler.cache;

import com.example.gorzdrav_spb_bot.handler.cache.dao.UserState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStateService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "user_state:";
    private static final long STATE_TTL_DAYS = 1;

    /**
     * Сохранить или обновить состояние пользователя
     */
    public void save(UserState userState) {
        String key = KEY_PREFIX + userState.getUserId();
        try {
            String jsonState = objectMapper.writeValueAsString(userState);
            redisTemplate.opsForValue().set(key, jsonState, STATE_TTL_DAYS, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации UserState для пользователя {}", userState.getUserId(), e);
        }
    }

    /**
     * Получить состояние пользователя
     */
    public Optional<UserState> findByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        String jsonState = redisTemplate.opsForValue().get(key);
        
        if (jsonState == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(jsonState, UserState.class));
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации UserState для пользователя {}", userId, e);
            return Optional.empty();
        }
    }

    /**
     * Удалить состояние (например, когда пользователь завершил сценарий)
     */
    public void deleteByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
