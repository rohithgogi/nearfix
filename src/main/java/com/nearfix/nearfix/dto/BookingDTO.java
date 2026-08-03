package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.BookingStatus;
import com.nearfix.nearfix.entity.PaymentStatus;
import com.nearfix.nearfix.entity.UrgencyLevel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;

    // Customer info
    private Long customerId;
    private String customerPhone;

    // Provider info
    private Long providerId;
    private String providerBusinessName;
    private String providerPhone;
    private String providerPhotoUrl;

    // Service info
    private Long serviceId;
    private String serviceName;
    private String serviceIcon;

    // Booking details
    private LocalDateTime scheduledDateTime;
    private String customerAddress;
    private BigDecimal customerLat;
    private BigDecimal customerLng;
    private String description;
    private List<String> photoUrls;
    private List<String> issueTags;
    private UrgencyLevel urgency;

    // Status and pricing
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal quotedPrice;
    private BigDecimal finalPrice;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // Helper flags
    private Boolean canBeCancelled;
    private Boolean canBeAccepted;
    private Boolean canBeCompleted;
}