package com.nearfix.nearfix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long bookingId;

    // Customer info (anonymized for privacy)
    private String customerName; // e.g., "Rajesh K."
    private String customerPhone; // Only show to provider

    // Provider info
    private Long providerId;
    private String providerBusinessName;

    // Service info
    private String serviceName;
    private String serviceIcon;

    // Review details
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    // Helper flag
    private Boolean canEdit; // Can customer edit their review?
}
