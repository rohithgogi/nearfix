package com.nearfix.nearfix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnsService {

    private final SnsClient snsClient;

    @Value("${aws.sns.sender-id}")
    private String senderId;

    @Value("${aws.sns.enabled:true}")
    private boolean snsEnabled;

    @Value("${aws.sns.mock-mode:false}")
    private boolean mockMode;

    public void sendSms(String phoneNumber, String message) {
        if (!snsEnabled) {
            log.info("SNS is disabled. SMS not sent to: {}", phoneNumber);
            return;
        }

        if (mockMode) {
            log.info("MOCK MODE: SMS would be sent to {} with message: {}", phoneNumber, message);
            return;
        }

        try {
            // Format phone number for international format
            String formattedPhone = formatPhoneNumber(phoneNumber);

            // Set SMS attributes
            Map<String, MessageAttributeValue> attributes = new HashMap<>();

            // SMS Type: Transactional (for OTPs)
            attributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                    .stringValue("Transactional")
                    .dataType("String")
                    .build());

            // Sender ID (visible to recipient)
            attributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                    .stringValue(senderId)
                    .dataType("String")
                    .build());

            // Build and send request
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(formattedPhone)
                    .messageAttributes(attributes)
                    .build();

            PublishResponse response = snsClient.publish(request);

            log.info("SMS sent successfully to {}. MessageId: {}", phoneNumber, response.messageId());

        } catch (SnsException e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send SMS: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    // Format phone number to E.164 format (+91XXXXXXXXXX)
    private String formatPhoneNumber(String phoneNumber) {
        // Remove any spaces, dashes, or special characters
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");

        // If doesn't start with +, add +91 for India
        if (!cleaned.startsWith("+")) {
            if (cleaned.startsWith("91")) {
                cleaned = "+" + cleaned;
            } else {
                cleaned = "+91" + cleaned;
            }
        }

        return cleaned;
    }

    // Send OTP specifically
    public void sendOtp(String phoneNumber, String otp) {
        String message = String.format(
                "Your NearFix OTP is: %s. Valid for 5 minutes. Do not share this code with anyone.",
                otp
        );
        sendSms(phoneNumber, message);
    }
}