package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementDTO {
    private Long userId;
    private String phoneNumber;
    private UserRole role;
    private String city;
    private Boolean phoneVerified;
    private Boolean active;

    // Additional Info for Providers
    private String businessName;
    private Boolean profileCompleted;
    private String verificationStatus;

    // Statistics
    private Integer totalBookings;
    private Integer completedBookings;

    // Timestamps
    private LocalDateTime registeredAt;
    private LocalDateTime lastActiveAt;
}