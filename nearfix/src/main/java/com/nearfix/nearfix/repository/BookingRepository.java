package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.Booking;
import com.nearfix.nearfix.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId " +
            "AND b.status IN :statuses ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerIdAndStatusIn(
            @Param("customerId") Long customerId,
            @Param("statuses") List<BookingStatus> statuses);

    // Provider queries
    List<Booking> findByProviderIdOrderByScheduledDateTimeDesc(Long providerId);

    List<Booking> findByProviderIdAndStatusOrderByScheduledDateTimeDesc(
            Long providerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.provider.id = :providerId " +
            "AND b.status IN :statuses ORDER BY b.scheduledDateTime DESC")
    List<Booking> findByProviderIdAndStatusIn(
            @Param("providerId") Long providerId,
            @Param("statuses") List<BookingStatus> statuses);

    // Upcoming bookings for provider
    @Query("SELECT b FROM Booking b WHERE b.provider.id = :providerId " +
            "AND b.status IN ('ACCEPTED', 'IN_PROGRESS') " +
            "AND b.scheduledDateTime >= :now " +
            "ORDER BY b.scheduledDateTime ASC")
    List<Booking> findUpcomingBookingsForProvider(
            @Param("providerId") Long providerId,
            @Param("now") LocalDateTime now);

    // Count bookings by status
    Long countByProviderIdAndStatus(Long providerId, BookingStatus status);

    Long countByCustomerIdAndStatus(Long customerId, BookingStatus status);

    // Find booking with validation
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId " +
            "AND (b.customer.id = :userId OR b.provider.user.id = :userId)")
    Optional<Booking> findByIdAndUserId(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId);
}

