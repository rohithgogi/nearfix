package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderDetailDTO {
    private Long providerId;
    private String businessName;
    private String photoUrl;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalBookings;
    private String address;
    private String city;
    private String pincode;
    private String bio;
    private Integer experienceYears;
    private String workingHours;
    private VerificationStatus verificationStatus;
    private List<ProviderSearchResultDTO.ServiceOfferedDTO> services;
    private List<ReviewDTO> recentReviews; // Placeholder for now

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDTO {
        private String customerName;
        private BigDecimal rating;
        private String comment;
        private String createdAt;
    }
}