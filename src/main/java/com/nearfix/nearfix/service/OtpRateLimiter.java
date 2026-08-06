package com.nearfix.nearfix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpRateLimiter {

    // Uses StringRedisTemplate (plain string serialization) rather than the app's
    // main RedisTemplate<String, Object>, which serializes values as JSON. A raw
    // Redis INCR command requires the stored value to literally be a numeric string —
    // JSON-wrapped values ("\"1\"" or similar) make INCR fail with
    // "ERR value is not an integer or out of range".
    private final StringRedisTemplate redisTemplate;

    private static final String COOLDOWN_PREFIX = "otp:cooldown:";
    private static final String HOURLY_PREFIX = "otp:hourly:";
    private static final int MAX_ATTEMPTS_PER_HOUR = 3;
    private static final int COOLDOWN_SECONDS = 60;

    public boolean canSendOtp(String phoneNumber) {
        // Check cooldown
        if (Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_PREFIX + phoneNumber))) {
            Long ttl = redisTemplate.getExpire(COOLDOWN_PREFIX + phoneNumber);
            log.warn("OTP cooldown active for {}: {} seconds remaining", phoneNumber, ttl);
            return false;
        }

        // Check hourly limit
        String hourlyKey = HOURLY_PREFIX + phoneNumber;
        Object countObj = redisTemplate.opsForValue().get(hourlyKey);
        int count = countObj != null ? Integer.parseInt(countObj.toString()) : 0;

        if (count >= MAX_ATTEMPTS_PER_HOUR) {
            log.warn("OTP rate limit exceeded for {}", phoneNumber);
            return false;
        }

        // Record attempt
        if (count == 0) {
            redisTemplate.opsForValue().set(hourlyKey, "1", Duration.ofHours(1));
        } else {
            redisTemplate.opsForValue().increment(hourlyKey);
        }

        // Set cooldown
        redisTemplate.opsForValue().set(
                COOLDOWN_PREFIX + phoneNumber, "1", Duration.ofSeconds(COOLDOWN_SECONDS));

        log.info("OTP allowed for {}: {}/{} attempts used", phoneNumber, count + 1, MAX_ATTEMPTS_PER_HOUR);
        return true;
    }

    public long getCooldownSeconds(String phoneNumber) {
        Long ttl = redisTemplate.getExpire(COOLDOWN_PREFIX + phoneNumber);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    public int getRemainingAttempts(String phoneNumber) {
        Object countObj = redisTemplate.opsForValue().get(HOURLY_PREFIX + phoneNumber);
        int count = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
        return Math.max(0, MAX_ATTEMPTS_PER_HOUR - count);
    }

    public void clearRateLimit(String phoneNumber) {
        redisTemplate.delete(COOLDOWN_PREFIX + phoneNumber);
        redisTemplate.delete(HOURLY_PREFIX + phoneNumber);
        log.info("Rate limit cleared for {}", phoneNumber);
    }
}