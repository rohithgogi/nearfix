package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.ProviderReviewStats;
import com.nearfix.nearfix.dto.ReviewDTO;
import com.nearfix.nearfix.dto.SubmitReviewRequest;
import com.nearfix.nearfix.security.JwtTokenProvider;
import com.nearfix.nearfix.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5175")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Submit a review (Customer only)
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> submitReview(
            HttpServletRequest request,
            @Valid @RequestBody SubmitReviewRequest reviewRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            log.info("Review submission request from: {}", phoneNumber);

            ReviewDTO review = reviewService.submitReview(phoneNumber, reviewRequest);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("Error submitting review: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get reviews for a provider with pagination
     * GET /api/reviews/provider/{providerId}?page=0&size=10
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<ReviewDTO>> getProviderReviews(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching reviews for provider: {}", providerId);
            Page<ReviewDTO> reviews = reviewService.getProviderReviews(providerId, page, size);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching reviews: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get recent reviews for a provider (limit 5)
     * GET /api/reviews/provider/{providerId}/recent
     */
    @GetMapping("/provider/{providerId}/recent")
    public ResponseEntity<List<ReviewDTO>> getRecentProviderReviews(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            log.info("Fetching recent {} reviews for provider: {}", limit, providerId);
            List<ReviewDTO> reviews = reviewService.getRecentProviderReviews(providerId, limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching recent reviews: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get review statistics for a provider
     * GET /api/reviews/provider/{providerId}/stats
     */
    @GetMapping("/provider/{providerId}/stats")
    public ResponseEntity<ProviderReviewStats> getProviderStats(
            @PathVariable Long providerId) {
        try {
            log.info("Fetching review stats for provider: {}", providerId);
            ProviderReviewStats stats = reviewService.getProviderStats(providerId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching review stats: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get review for a specific booking
     * GET /api/reviews/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewDTO> getReviewByBooking(
            HttpServletRequest request,
            @PathVariable Long bookingId) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            ReviewDTO review = reviewService.getReviewByBookingId(phoneNumber, bookingId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("Error fetching review: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getPhoneNumberFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtTokenProvider.getPhoneNumberFromToken(token);
        }
        throw new RuntimeException("No authentication token found");
    }
}