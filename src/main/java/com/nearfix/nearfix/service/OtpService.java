package com.nearfix.nearfix.service;

import com.nearfix.nearfix.entity.Otp;
import com.nearfix.nearfix.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpRateLimiter rateLimiter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsService smsService;

    private static final String OTP_PREFIX = "otp:";
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    public void sendOtp(String phoneNumber) {
        if (!rateLimiter.canSendOtp(phoneNumber)) {
            long cooldown = rateLimiter.getCooldownSeconds(phoneNumber);
            int remaining = rateLimiter.getRemainingAttempts(phoneNumber);
            if (cooldown > 0) {
                throw new RuntimeException(
                        String.format("Please wait %d seconds before requesting another OTP", cooldown));
            } else {
                throw new RuntimeException(
                        String.format("OTP limit exceeded. Try again later. (%d remaining today)", remaining));
            }
        }

        String code = String.format("%04d", new Random().nextInt(10000));

        // Store in Redis with auto-expiry instead of MySQL
        redisTemplate.opsForValue().set(OTP_PREFIX + phoneNumber, code, OTP_TTL);

        log.info("OTP generated for {}: {}", phoneNumber, code);

        try {
            smsService.sendOtp(phoneNumber, code);
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    public boolean verifyOtp(String phoneNumber, String otpCode) {
        String key = OTP_PREFIX + phoneNumber;
        Object stored = redisTemplate.opsForValue().get(key);

        if (stored == null) {
            log.warn("OTP not found or expired for {}", phoneNumber);
            return false;
        }

        if (!stored.toString().equals(otpCode)) {
            log.warn("OTP mismatch for {}", phoneNumber);
            return false;
        }

        // Delete after successful verification (one-time use)
        redisTemplate.delete(key);
        log.info("OTP verified successfully for {}", phoneNumber);
        return true;
    }
}