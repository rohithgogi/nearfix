package com.nearfix.nearfix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends SMS via two different MSG91 endpoints depending on message type:
 * <p>
 * - OTP messages use MSG91's dedicated OTP API (https://control.msg91.com/api/v5/otp).
 *   If msg91.otp-template-id is left blank, MSG91 falls back to its own default OTP
 *   template — this works without any DLT registration, so it's the easiest way to
 *   get OTP sending working while you're still setting up DLT. Once you have your
 *   own DLT-approved OTP template, set msg91.otp-template-id to use your own wording.
 * <p>
 * - Free-text notifications (booking created/accepted/etc.) use the Flow API
 *   (https://control.msg91.com/api/v5/flow/), which always requires a DLT-approved
 *   template (msg91.notification-template-id) — there's no default-template shortcut
 *   for this one, since it's TRAI-mandated for all transactional/promotional SMS in
 *   India. Leave msg91.notification-template-id blank until DLT is done; notifications
 *   will just be logged instead of sent.
 * <p>
 * If msg91.enabled=false or the authkey is missing, everything is logged instead of
 * calling the API, so local/dev environments keep working without MSG91 credentials.
 */
@Service
@Slf4j
public class Msg91SmsService implements SmsService {

    private static final String FLOW_URL = "https://control.msg91.com/api/v5/flow/";
    private static final String OTP_URL = "https://control.msg91.com/api/v5/otp";

    @Value("${msg91.authkey:}")
    private String authKey;

    // Optional: leave blank to use MSG91's default OTP template (no DLT approval required
    // to get started, but the wording isn't customizable and there's no branding).
    // Once you complete DLT registration and get your own OTP template approved, set this
    // and OTP sends will use your custom wording instead.
    @Value("${msg91.otp-template-id:}")
    private String otpTemplateId;

    @Value("${msg91.notification-template-id:}")
    private String notificationTemplateId;

    @Value("${msg91.country-code:91}")
    private String countryCode;

    @Value("${msg91.enabled:true}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    public Msg91SmsService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        if (!enabled) {
            log.info("MSG91 disabled (msg91.enabled=false) — would send OTP {} to {}", otpCode, phoneNumber);
            return;
        }
        if (isBlank(authKey)) {
            log.warn("msg91.authkey not configured; skipping MSG91 OTP send to {}", phoneNumber);
            return;
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(OTP_URL)
                    .queryParam("mobile", normalizeMobile(phoneNumber))
                    .queryParam("otp", otpCode)
                    .queryParam("otp_length", otpCode.length());
            if (!isBlank(otpTemplateId)) {
                builder.queryParam("template_id", otpTemplateId);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("authkey", authKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.postForEntity(builder.toUriString(), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("MSG91 OTP dispatched to {}", phoneNumber);
            } else {
                log.error("MSG91 OTP failed for {} - status {}: {}",
                        phoneNumber, response.getStatusCode(), response.getBody());
            }
        } catch (RestClientException e) {
            log.error("MSG91 OTP request failed for {}: {}", phoneNumber, e.getMessage());
        }
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        if (isBlank(notificationTemplateId)) {
            log.warn("msg91.notification-template-id not configured; skipping MSG91 SMS send to {}", phoneNumber);
            return;
        }
        sendViaFlow(notificationTemplateId, phoneNumber, Map.of("VAR1", message));
    }

    private void sendViaFlow(String templateId, String phoneNumber, Map<String, String> variables) {
        if (!enabled) {
            log.info("MSG91 disabled (msg91.enabled=false) — would send to {} vars={}", phoneNumber, variables);
            return;
        }
        if (isBlank(authKey)) {
            log.warn("msg91.authkey not configured; skipping MSG91 SMS send to {}", phoneNumber);
            return;
        }

        try {
            Map<String, Object> recipient = new HashMap<>(variables);
            recipient.put("mobiles", normalizeMobile(phoneNumber));

            Map<String, Object> body = new HashMap<>();
            body.put("template_id", templateId);
            body.put("short_url", "0");
            body.put("recipients", List.of(recipient));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", authKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(FLOW_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("MSG91 SMS dispatched to {}", phoneNumber);
            } else {
                log.error("MSG91 SMS failed for {} - status {}: {}",
                        phoneNumber, response.getStatusCode(), response.getBody());
            }
        } catch (RestClientException e) {
            log.error("MSG91 SMS request failed for {}: {}", phoneNumber, e.getMessage());
        }
    }

    /**
     * MSG91 expects mobile numbers with country code and no leading '+', e.g. 919999999999.
     */
    private String normalizeMobile(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return countryCode + digits;
        }
        return digits;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}