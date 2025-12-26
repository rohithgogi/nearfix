package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderProfileDTO {
    private Long id;
    private String phoneNumber;

    // Profile Information
    private String businessName;
    private String address;
    private String city;
    private String pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String photoUrl;
    private String aadharUrl;
    private String workingHours;  // JSON string
    private String bio;

    // Status
    private VerificationStatus verificationStatus;
    private Boolean profileCompleted;
    private Integer profileCompletionPercentage;

    // Legacy fields
    private Integer experienceYears;
    private String availabilityStatus;
}