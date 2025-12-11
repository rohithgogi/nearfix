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
@Table(name = "bookings", indexes = {
        @Index(name = "idx_customer_bookings", columnList = "customer_id,created_at"),
        @Index(name = "idx_provider_bookings", columnList = "provider_id,scheduled_date_time"),
        @Index(name = "idx_booking_status", columnList = "status,scheduled_date_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String customerAddress;

    @Column(precision = 10, scale = 8)
    private BigDecimal customerLat;

    @Column(precision = 11, scale = 8)
    private BigDecimal customerLng;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(precision = 10, scale = 2)
    private BigDecimal quotedPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    // Helper methods
    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.ACCEPTED;
    }

    public boolean canBeAccepted() {
        return status == BookingStatus.PENDING;
    }

    public boolean canBeCompleted() {
        return status == BookingStatus.ACCEPTED || status == BookingStatus.IN_PROGRESS;
    }
}