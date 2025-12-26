package com.nearfix.nearfix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_provider_reviews", columnList = "provider_id,created_at"),
        @Index(name = "idx_booking_review", columnList = "booking_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id",nullable = false,unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id",nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void validateRating(){
        if(rating==null || rating<1 ||rating >5){
            throw new IllegalArgumentException("Rating should be between 1 to 5");
        }
    }


}
