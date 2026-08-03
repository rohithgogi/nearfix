package com.nearfix.nearfix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Photo evidence of the problem - lets the provider see before they arrive
    @ElementCollection
    @CollectionTable(name = "booking_photos", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "photo_url", length = 500)
    private List<String> photoUrls = new ArrayList<>();

    // Quick-tap issue tags, e.g. "No power", "Sparking", "Leaking" - static list per service on the frontend for now
    @ElementCollection
    @CollectionTable(name = "booking_issue_tags", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "tag", length = 100)
    private List<String> issueTags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UrgencyLevel urgency = UrgencyLevel.MEDIUM;

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

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to check if payment is completed
    public boolean isPaid() {
        return razorpayPaymentId != null && paidAt != null;
    }

    // Helper method to check if booking can be paid
    public boolean canBePaid() {
        return status == BookingStatus.COMPLETED
                && razorpayPaymentId == null
                && finalPrice != null;
    }

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