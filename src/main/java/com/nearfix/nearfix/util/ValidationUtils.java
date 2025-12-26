package com.nearfix.nearfix.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Common validation utilities
 * Centralized validation logic for consistency
 */
public class ValidationUtils {

    // Indian phone number pattern: 10 digits starting with 6-9
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    // Indian pincode pattern: 6 digits, first digit 1-9
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[1-9][0-9]{5}$");

    /**
     * Validate Indian mobile number
     * Must be 10 digits starting with 6-9
     */
    public static void validatePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        String cleaned = phone.replaceAll("[^0-9]", "");

        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone number. Must be 10 digits starting with 6-9"
            );
        }
    }

    /**
     * Validate price (must be positive)
     */
    public static void validatePrice(BigDecimal price, String fieldName) {
        if (price == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }

        if (price.compareTo(new BigDecimal("99999.99")) > 0) {
            throw new IllegalArgumentException(fieldName + " is too high (max ₹99,999.99)");
        }
    }

    /**
     * Validate date is in future (with buffer)
     */
    public static void validateFutureDate(LocalDateTime dateTime, String fieldName) {
        if (dateTime == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        // Allow booking up to 1 hour in the past (for timezone issues)
        LocalDateTime minTime = LocalDateTime.now().minusHours(1);

        if (dateTime.isBefore(minTime)) {
            throw new IllegalArgumentException(fieldName + " must be in the future");
        }

        // Don't allow bookings more than 90 days in advance
        LocalDateTime maxTime = LocalDateTime.now().plusDays(90);
        if (dateTime.isAfter(maxTime)) {
            throw new IllegalArgumentException(fieldName + " cannot be more than 90 days in advance");
        }
    }

    /**
     * Validate Indian pincode
     */
    public static void validatePincode(String pincode) {
        if (pincode == null || pincode.isBlank()) {
            throw new IllegalArgumentException("Pincode is required");
        }

        if (!PINCODE_PATTERN.matcher(pincode).matches()) {
            throw new IllegalArgumentException("Invalid pincode. Must be 6 digits");
        }
    }

    /**
     * Validate latitude
     */
    public static void validateLatitude(BigDecimal latitude) {
        if (latitude == null) {
            return; // Optional field
        }

        if (latitude.compareTo(new BigDecimal("-90")) < 0 ||
                latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
    }

    /**
     * Validate longitude
     */
    public static void validateLongitude(BigDecimal longitude) {
        if (longitude == null) {
            return; // Optional field
        }

        if (longitude.compareTo(new BigDecimal("-180")) < 0 ||
                longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    /**
     * Validate rating (1-5)
     */
    public static void validateRating(Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating is required");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * Validate string length
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            if (minLength > 0) {
                throw new IllegalArgumentException(fieldName + " is required");
            }
            return;
        }

        if (value.length() < minLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be at least " + minLength + " characters"
            );
        }

        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be at most " + maxLength + " characters"
            );
        }
    }
}