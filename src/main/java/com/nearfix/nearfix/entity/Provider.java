package com.nearfix.nearfix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "providers", indexes = {
        @Index(name = "idx_verified_available", columnList = "verified,availability_status"),
        @Index(name = "idx_location", columnList = "latitude,longitude"),
        @Index(name = "idx_profile_completed", columnList = "profile_completed")
})
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Legacy field - keep for backward compatibility
    private String serviceType;

    // Profile Information
    private String businessName;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;
    private String pincode;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 500)
    private String photoUrl;

    @Column(length = 500)
    private String aadharUrl;

    @Column(columnDefinition = "JSON")
    private String workingHours;  // Store as JSON string

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(nullable = false)
    private Boolean profileCompleted = false;

    // Rating and Stats (NEW FIELDS)
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalReviews = 0;

    @Column(nullable = false)
    private Integer totalBookings = 0;

    // Legacy fields
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    private Boolean verified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to calculate profile completion
    public int getProfileCompletionPercentage() {
        int total = 0;
        int completed = 0;

        // Business Name (20%)
        total += 20;
        if (businessName != null && !businessName.trim().isEmpty()) {
            completed += 20;
        }

        // Address & Location (20%)
        total += 20;
        if (address != null && !address.trim().isEmpty() &&
                city != null && pincode != null &&
                latitude != null && longitude != null) {
            completed += 20;
        }

        // Photo (20%)
        total += 20;
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            completed += 20;
        }

        // Documents (20%)
        total += 20;
        if (aadharUrl != null && !aadharUrl.trim().isEmpty()) {
            completed += 20;
        }

        // Working Hours (20%)
        total += 20;
        if (workingHours != null && !workingHours.trim().isEmpty()) {
            completed += 20;
        }

        return (total == 0) ? 0 : (completed * 100 / total);
    }
}