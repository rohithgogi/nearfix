package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderVerificationDTO {
    private Long providerId;
    private String phoneNumber;

    // Profile Info
    private String businessName;
    private String address;
    private String city;
    private String pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Documents
    private String photoUrl;
    private String aadharUrl;

    // Profile Details
    private String bio;
    private String workingHours;
    private Integer experienceYears;

    // Verification Status
    private VerificationStatus verificationStatus;
    private Integer profileCompletionPercentage;
    private Boolean profileCompleted;

    // Services Offered
    private List<ServiceOfferedDTO> services;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Stats
    private Integer totalBookings;
    private BigDecimal rating;
    private Integer totalReviews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceOfferedDTO {
        private Long serviceId;
        private String serviceName;
        private String serviceIcon;
        private BigDecimal basePrice;
        private Integer experienceYears;
    }
}