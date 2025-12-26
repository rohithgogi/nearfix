package com.nearfix.nearfix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for OTP requests
 * Prevents SMS bombing and abuse
 */
@Component
@Slf4j
public class OtpRateLimiter {

    // Track OTP requests per phone number
    private final Map<String, OtpAttemptTracker> attemptTrackers = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS_PER_HOUR = 3;
    private static final int COOLDOWN_SECONDS = 60;  // 1 minute between OTPs

    /**
     * Check if OTP can be sent to this phone number
     * @param phoneNumber Phone number
     * @return true if allowed, false if rate limited
     */
    public boolean canSendOtp(String phoneNumber) {
        OtpAttemptTracker tracker = attemptTrackers.computeIfAbsent(
                phoneNumber,
                k -> new OtpAttemptTracker()
        );

        // Clean up old attempts (older than 1 hour)
        tracker.cleanupOldAttempts();

        // Check cooldown period
        if (tracker.isInCooldown()) {
            long secondsRemaining = tracker.getCooldownSecondsRemaining();
            log.warn("⚠️ OTP cooldown active for {}: {} seconds remaining",
                    phoneNumber, secondsRemaining);
            return false;
        }

        // Check hourly limit
        if (tracker.getAttemptsInLastHour() >= MAX_ATTEMPTS_PER_HOUR) {
            log.warn("⚠️ OTP rate limit exceeded for {}: {} attempts in last hour",
                    phoneNumber, tracker.getAttemptsInLastHour());
            return false;
        }

        // Record attempt
        tracker.recordAttempt();
        log.info("✅ OTP allowed for {}: {}/{} attempts used",
                phoneNumber, tracker.getAttemptsInLastHour(), MAX_ATTEMPTS_PER_HOUR);

        return true;
    }

    /**
     * Get remaining time before next OTP can be sent
     * @param phoneNumber Phone number
     * @return seconds remaining, or 0 if can send now
     */
    public long getCooldownSeconds(String phoneNumber) {
        OtpAttemptTracker tracker = attemptTrackers.get(phoneNumber);
        if (tracker == null) {
            return 0;
        }
        return tracker.isInCooldown() ? tracker.getCooldownSecondsRemaining() : 0;
    }

    /**
     * Get remaining attempts in current hour
     * @param phoneNumber Phone number
     * @return remaining attempts
     */
    public int getRemainingAttempts(String phoneNumber) {
        OtpAttemptTracker tracker = attemptTrackers.get(phoneNumber);
        if (tracker == null) {
            return MAX_ATTEMPTS_PER_HOUR;
        }
        tracker.cleanupOldAttempts();
        return Math.max(0, MAX_ATTEMPTS_PER_HOUR - tracker.getAttemptsInLastHour());
    }

    /**
     * Clear rate limit for a phone number (admin override)
     * @param phoneNumber Phone number
     */
    public void clearRateLimit(String phoneNumber) {
        attemptTrackers.remove(phoneNumber);
        log.info("✅ Rate limit cleared for {}", phoneNumber);
    }

    /**
     * Inner class to track OTP attempts for a phone number
     */
    private static class OtpAttemptTracker {
        private final Map<LocalDateTime, Boolean> attempts = new ConcurrentHashMap<>();
        private LocalDateTime lastAttempt;

        public void recordAttempt() {
            LocalDateTime now = LocalDateTime.now();
            attempts.put(now, true);
            lastAttempt = now;
        }

        public void cleanupOldAttempts() {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            attempts.keySet().removeIf(timestamp -> timestamp.isBefore(oneHourAgo));
        }

        public int getAttemptsInLastHour() {
            cleanupOldAttempts();
            return attempts.size();
        }

        public boolean isInCooldown() {
            if (lastAttempt == null) {
                return false;
            }
            LocalDateTime cooldownEnd = lastAttempt.plusSeconds(COOLDOWN_SECONDS);
            return LocalDateTime.now().isBefore(cooldownEnd);
        }

        public long getCooldownSecondsRemaining() {
            if (!isInCooldown()) {
                return 0;
            }
            LocalDateTime cooldownEnd = lastAttempt.plusSeconds(COOLDOWN_SECONDS);
            return java.time.Duration.between(LocalDateTime.now(), cooldownEnd).getSeconds();
        }
    }
}