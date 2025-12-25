package com.nearfix.nearfix.dto;

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
public class AdminDashboardStatsDTO {

    // User Statistics
    private Integer totalUsers;
    private Integer totalCustomers;
    private Integer totalProviders;
    private Integer newUsersThisMonth;

    // Provider Statistics
    private Integer pendingVerifications;
    private Integer verifiedProviders;
    private Integer rejectedProviders;

    // Booking Statistics
    private Integer totalBookings;
    private Integer pendingBookings;
    private Integer completedBookings;
    private Integer cancelledBookings;
    private Integer bookingsThisMonth;

    // Revenue Statistics
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private BigDecimal averageBookingValue;

    // Service Statistics
    private Integer totalServices;
    private Integer activeProviderServices;

    // Recent Activity
    private List<RecentActivityDTO> recentActivities;

    // Growth Metrics
    private Double userGrowthRate; // % month over month
    private Double bookingGrowthRate;
    private Double revenueGrowthRate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentActivityDTO {
        private String type; // BOOKING, REGISTRATION, VERIFICATION
        private String description;
        private LocalDateTime timestamp;
        private String userPhone;
        private Long entityId;
    }
}