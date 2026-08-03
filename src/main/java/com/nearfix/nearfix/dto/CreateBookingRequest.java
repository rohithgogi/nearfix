package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.BookingStatus;
import com.nearfix.nearfix.entity.PaymentStatus;
import com.nearfix.nearfix.entity.UrgencyLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Request to create booking
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Scheduled date/time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledDateTime;

    @NotBlank(message = "Customer address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10-500 characters")
    private String customerAddress;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private BigDecimal customerLat;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private BigDecimal customerLng;

    @Size(max = 1000, message = "Description too long (max 1000 characters)")
    private String description;

    @DecimalMin(value = "0.01", message = "Quoted price must be positive if provided")
    @DecimalMax(value = "99999.99", message = "Quoted price is too high")
    private BigDecimal quotedPrice; // Optional, for reference

    @Size(max = 3, message = "Maximum 3 photos allowed")
    private List<String> photoUrls; // Optional, URLs returned from /api/bookings/attachments

    @Size(max = 10, message = "Maximum 10 issue tags allowed")
    private List<String> issueTags; // Optional, quick-tap tags e.g. "No power", "Leaking"

    private UrgencyLevel urgency; // Optional, defaults to MEDIUM if not provided

    /**
     * Custom validation logic
     */
    public void validate() {
        // Validate future date with buffer
        if (scheduledDateTime != null) {
            LocalDateTime minTime = LocalDateTime.now().minusHours(1);
            if (scheduledDateTime.isBefore(minTime)) {
                throw new IllegalArgumentException("Scheduled time must be in the future");
            }

            LocalDateTime maxTime = LocalDateTime.now().plusDays(90);
            if (scheduledDateTime.isAfter(maxTime)) {
                throw new IllegalArgumentException("Cannot book more than 90 days in advance");
            }
        }

        // Validate coordinates
        if (customerLat != null && (customerLat.compareTo(new BigDecimal("-90")) < 0 ||
                customerLat.compareTo(new BigDecimal("90")) > 0)) {
            throw new IllegalArgumentException("Invalid latitude");
        }

        if (customerLng != null && (customerLng.compareTo(new BigDecimal("-180")) < 0 ||
                customerLng.compareTo(new BigDecimal("180")) > 0)) {
            throw new IllegalArgumentException("Invalid longitude");
        }
    }
}