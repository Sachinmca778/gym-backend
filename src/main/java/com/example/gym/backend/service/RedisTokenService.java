package com.example.gym.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRATION_HOURS = 24;

    /**
     * Store access token in Redis
     */
    public void storeAccessToken(String username, String accessToken) {
        String key = TOKEN_PREFIX + "access:" + username;
        redisTemplate.opsForValue().set(key, accessToken, TOKEN_EXPIRATION_HOURS, TimeUnit.HOURS);
        log.info("Stored access token for user: {}", username);
    }

    /**
     * Store refresh token in Redis
     */
    public void storeRefreshToken(String username, String refreshToken) {
        String key = TOKEN_PREFIX + "refresh:" + username;
        redisTemplate.opsForValue().set(key, refreshToken, 7 * TOKEN_EXPIRATION_HOURS, TimeUnit.HOURS);
        log.info("Stored refresh token for user: {}", username);
    }

    /**
     * Get access token from Redis
     */
    public String getAccessToken(String username) {
        String key = TOKEN_PREFIX + "access:" + username;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get refresh token from Redis
     */
    public String getRefreshToken(String username) {
        String key = TOKEN_PREFIX + "refresh:" + username;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Validate access token exists in Redis
     */
    public boolean validateAccessToken(String username, String accessToken) {
        String key = TOKEN_PREFIX + "access:" + username;
        Object storedToken = redisTemplate.opsForValue().get(key);
        return accessToken.equals(storedToken);
    }

    /**
     * Validate refresh token exists in Redis
     */
    public boolean validateRefreshToken(String username, String refreshToken) {
        String key = TOKEN_PREFIX + "refresh:" + username;
        Object storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    /**
     * Delete all tokens for a user (logout)
     */
    public void deleteTokens(String username) {
        String accessKey = TOKEN_PREFIX + "access:" + username;
        String refreshKey = TOKEN_PREFIX + "refresh:" + username;
        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
        log.info("Deleted all tokens for user: {}", username);
    }

    /**
     * Store user session data
     */
    public void storeUserSession(String username, Object sessionData) {
        String key = TOKEN_PREFIX + "session:" + username;
        redisTemplate.opsForValue().set(key, sessionData, TOKEN_EXPIRATION_HOURS, TimeUnit.HOURS);
        log.info("Stored session data for user: {}", username);
    }

    /**
     * Get user session data
     */
    public Object getUserSession(String username) {
        String key = TOKEN_PREFIX + "session:" + username;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Delete user session
     */
    public void deleteUserSession(String username) {
        String key = TOKEN_PREFIX + "session:" + username;
        redisTemplate.delete(key);
    }
}

