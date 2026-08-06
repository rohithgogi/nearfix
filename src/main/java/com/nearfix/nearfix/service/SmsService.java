package com.nearfix.nearfix.service;

/**
 * Abstraction over the SMS provider so callers (OtpService, NotificationService, etc.)
 * don't need to know which vendor (MSG91) is behind it.
 */
public interface SmsService {

    /**
     * Send a one-time-password to a phone number using the configured OTP template.
     */
    void sendOtp(String phoneNumber, String otpCode);

    /**
     * Send a free-text transactional notification to a phone number using the
     * configured generic notification template.
     */
    void sendSms(String phoneNumber, String message);
}