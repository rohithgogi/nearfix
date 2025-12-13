package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.ProviderReviewStats;
import com.nearfix.nearfix.dto.ReviewDTO;
import com.nearfix.nearfix.dto.SubmitReviewRequest;
import com.nearfix.nearfix.entity.*;
import com.nearfix.nearfix.repository.BookingRepository;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.ReviewRepository;
import com.nearfix.nearfix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    @Transactional
    public ReviewDTO submitReview(String customerPhone, SubmitReviewRequest request){
        log.info("Subitting review for booking: {} by customer: {}",request.getBookingId(),customerPhone);

        User customer=userRepository.findByPhoneNumber(customerPhone)
                .orElseThrow(()->new RuntimeException("Customer not found"));
        if(customer.getRole()!= UserRole.CUSTOMER){
            throw new RuntimeException("only customers can submit review");
        }

        Booking booking=bookingRepository.findById(request.getBookingId())
                .orElseThrow(()-> new RuntimeException("Booking not found"));
        if(!booking.getCustomer().getId().equals(customer.getId())){
            throw new RuntimeException("You can only give review to your own bookings");
        }
        if (booking.getStatus()!= BookingStatus.COMPLETED){
            throw new RuntimeException("You can only review completed bookings");
        }
        if(reviewRepository.existsByBookingId(booking.getId())){
            throw new RuntimeException("You have already reviewed this booking");
        }

        Review review=new Review();
        review.setBooking(booking);
        review.setCustomer(customer);
        review.setProvider(booking.getProvider());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.validateRating();

        review = reviewRepository.save(review);
        log.info("✅ Review created with ID: {}", review.getId());

        // 4. Update provider's rating
        updateProviderRating(booking.getProvider().getId());

        return convertToDTO(review, customer.getId());
    }

    @Transactional
    public void updateProviderRating(Long providerId){
        log.info("Updating rating for provider: {}", providerId);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Calculate new average rating
        Double avgRating = reviewRepository.calculateAverageRating(providerId);
        Long totalReviews = reviewRepository.countByProviderId(providerId);

        BigDecimal roundedRating= avgRating!=null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                :BigDecimal.ZERO;

        provider.setRating(roundedRating);
        provider.setTotalReviews(totalReviews.intValue());
        providerRepository.save(provider);

        log.info("✅ Provider rating updated: {} stars ({} reviews)", roundedRating, totalReviews);

    }

    public Page<ReviewDTO> getProviderReviews(Long providerId, int page,int size){
        log.info("Fetching reviews for provider: {} (page: {}, size: {})", providerId, page, size);


        Pageable pageable= PageRequest.of(page,size);
        Page<Review> reviews=reviewRepository.findByProviderIdOrderByCreatedAtDesc(providerId,pageable);

        return reviews.map(review -> convertToDTO(review,null));
    }

    public List<ReviewDTO> getRecentProviderReviews(Long providerId, int limit){
        log.info("Fetching recent {} reviews for provider: {}", limit, providerId);

        Pageable pageable = PageRequest.of(0, limit);
        Page<Review> reviews = reviewRepository.findRecentProviderReviews(providerId, pageable);

        return reviews.getContent().stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }

    public ProviderReviewStats getProviderStats(Long providerId) {
        log.info("Calculating review stats for provider: {}", providerId);

        List<Review> allReviews = reviewRepository.findByProviderIdOrderByCreatedAtDesc(
                providerId,
                Pageable.unpaged()
        ).getContent();

        // Count ratings by star
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }

        for (Review review : allReviews) {
            int rating = review.getRating();
            ratingCounts.put(rating, ratingCounts.get(rating) + 1);
        }

        Double avgRating = reviewRepository.calculateAverageRating(providerId);
        Long totalReviews = reviewRepository.countByProviderId(providerId);

        return ProviderReviewStats.builder()
                .providerId(providerId)
                .averageRating(avgRating != null ?
                        BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP).doubleValue()
                        : 0.0)
                .totalReviews(totalReviews.intValue())
                .fiveStarCount(ratingCounts.get(5).intValue())
                .fourStarCount(ratingCounts.get(4).intValue())
                .threeStarCount(ratingCounts.get(3).intValue())
                .twoStarCount(ratingCounts.get(2).intValue())
                .oneStarCount(ratingCounts.get(1).intValue())
                .build();
    }

    public ReviewDTO getReviewByBookingId(String userPhone, Long bookingId) {
        User user = userRepository.findByPhoneNumber(userPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Check access permission
        if (!review.getCustomer().getId().equals(user.getId()) &&
                !review.getProvider().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return convertToDTO(review, user.getId());
    }

    public ReviewDTO convertToDTO(Review review,Long viewerId){
        String customerName=anonymizeCustomerName(review.getCustomer().getPhoneNumber());

        return ReviewDTO.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .customerName(customerName)
                .customerPhone(review.getCustomer().getPhoneNumber())
                .providerId(review.getProvider().getId())
                .providerBusinessName(review.getProvider().getBusinessName())
                .serviceName(review.getBooking().getService().getName())
                .serviceIcon(review.getBooking().getService().getIconEmoji())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .canEdit(viewerId != null && viewerId.equals(review.getCustomer().getId()))
                .build();


    }

    public String anonymizeCustomerName(String phoneNumber){
        if(phoneNumber!=null && phoneNumber.length()>=10){
            String lastThree=phoneNumber.substring(phoneNumber.length()-3);
            return "Customer "+lastThree;
        }
        return "Customer";
    }
}
