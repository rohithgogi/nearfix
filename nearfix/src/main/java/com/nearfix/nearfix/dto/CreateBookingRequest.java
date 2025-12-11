package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.BookingStatus;
import com.nearfix.nearfix.entity.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String customerAddress;

    @NotNull(message = "Latitude is required")
    private BigDecimal customerLat;

    @NotNull(message = "Longitude is required")
    private BigDecimal customerLng;

    @Size(max = 1000, message = "Description too long")
    private String description;

    private BigDecimal quotedPrice; // Optional, for reference
}