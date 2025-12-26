package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ FIXED: Correct method name
    boolean existsByBookingId(Long bookingId);

    // Find review by booking ID
    Optional<Review> findByBookingId(Long bookingId);

    // Get provider reviews with pagination
    Page<Review> findByProviderIdOrderByCreatedAtDesc(Long providerId, Pageable pageable);

    // Get customer reviews
    Page<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    // Calculate average rating for a provider
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider.id = :providerId")
    Double calculateAverageRating(@Param("providerId") Long providerId);

    // Count total reviews for a provider
    Long countByProviderId(Long providerId);

    // Get recent reviews for provider (limit 5)
    @Query("SELECT r FROM Review r WHERE r.provider.id = :providerId ORDER BY r.createdAt DESC")
    Page<Review> findRecentProviderReviews(@Param("providerId") Long providerId, Pageable pageable);
}