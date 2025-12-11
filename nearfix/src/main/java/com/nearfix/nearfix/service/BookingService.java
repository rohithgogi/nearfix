package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.BookingActionRequest;
import com.nearfix.nearfix.dto.BookingDTO;
import com.nearfix.nearfix.dto.CompleteBookingRequest;
import com.nearfix.nearfix.dto.CreateBookingRequest;
import com.nearfix.nearfix.entity.*;
import com.nearfix.nearfix.repository.BookingRepository;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.ServiceRepository;
import com.nearfix.nearfix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;


    @Transactional
    public BookingDTO createBooking(String customerPhone, CreateBookingRequest request){
        log.info("Creating booking for customer: {}",customerPhone);

        //validate customer
        User customer=userRepository.findByPhoneNumber(customerPhone)
                .orElseThrow(()-> new RuntimeException("Customer not found"));

        if(customer.getRole()!= UserRole.CUSTOMER){
            throw new RuntimeException("Only customers can create bookings");
        }

        //validate provider
        Provider provider=providerRepository.findById(request.getProviderId())
                .orElseThrow(()->new RuntimeException("Provider not found"));
        if (!provider.getVerified() || !provider.getProfileCompleted()) {
            throw new RuntimeException("Provider is not available for bookings");
        }

        //validate service
        com.nearfix.nearfix.entity.Service service=serviceRepository.findById(request.getServiceId())
                .orElseThrow(()->new RuntimeException("Service not found"));
        //validate scheduled time in future
        if(request.getScheduledDateTime().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Scheduled time must be in the future");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setProvider(provider);
        booking.setService(service);
        booking.setScheduledDateTime(request.getScheduledDateTime());
        booking.setCustomerAddress(request.getCustomerAddress());
        booking.setCustomerLat(request.getCustomerLat());
        booking.setCustomerLng(request.getCustomerLng());
        booking.setDescription(request.getDescription());
        booking.setQuotedPrice(request.getQuotedPrice());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        booking = bookingRepository.save(booking);
        log.info("✅ Booking created successfully with ID: {}", booking.getId());

        // Send notification to provider
        notificationService.notifyBookingCreated(booking);

        return convertToDTO(booking);

    }
    public List<BookingDTO> getCustomerBookings(String customerPhone, String statusFilter) {
        log.info("Fetching bookings for customer: {}, filter: {}", customerPhone, statusFilter);

        User customer = userRepository.findByPhoneNumber(customerPhone)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Booking> bookings;

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("all")) {
            try {
                BookingStatus status = BookingStatus.valueOf(statusFilter.toUpperCase());
                bookings = bookingRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                        customer.getId(), status);
            } catch (IllegalArgumentException e) {
                // Handle multiple statuses (e.g., "active" = PENDING + ACCEPTED)
                if (statusFilter.equalsIgnoreCase("active")) {
                    bookings = bookingRepository.findByCustomerIdAndStatusIn(
                            customer.getId(),
                            Arrays.asList(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS)
                    );
                } else {
                    bookings = bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
                }
            }
        } else {
            bookings = bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        }

        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getProviderBookings(String providerPhone, String statusFilter) {
        log.info("Fetching bookings for provider: {}, filter: {}", providerPhone, statusFilter);

        User providerUser = userRepository.findByPhoneNumber(providerPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Provider provider = providerRepository.findByUserId(providerUser.getId())
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        List<Booking> bookings;

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("all")) {
            try {
                BookingStatus status = BookingStatus.valueOf(statusFilter.toUpperCase());
                bookings = bookingRepository.findByProviderIdAndStatusOrderByScheduledDateTimeDesc(
                        provider.getId(), status);
            } catch (IllegalArgumentException e) {
                if (statusFilter.equalsIgnoreCase("upcoming")) {
                    bookings = bookingRepository.findUpcomingBookingsForProvider(
                            provider.getId(), LocalDateTime.now());
                } else {
                    bookings = bookingRepository.findByProviderIdOrderByScheduledDateTimeDesc(provider.getId());
                }
            }
        } else {
            bookings = bookingRepository.findByProviderIdOrderByScheduledDateTimeDesc(provider.getId());
        }

        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO acceptBooking(String providerPhone, Long bookingId) {
        log.info("Provider {} accepting booking {}", providerPhone, bookingId);

        Booking booking = getBookingForProvider(providerPhone, bookingId);

        if (!booking.canBeAccepted()) {
            throw new RuntimeException("Booking cannot be accepted in current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        booking = bookingRepository.save(booking);

        // Notify customer
        notificationService.notifyBookingAccepted(booking);

        log.info("✅ Booking {} accepted", bookingId);
        return convertToDTO(booking);
    }

    @Transactional
    public BookingDTO rejectBooking(String providerPhone, Long bookingId, BookingActionRequest request) {
        log.info("Provider {} rejecting booking {}", providerPhone, bookingId);

        Booking booking = getBookingForProvider(providerPhone, bookingId);

        if (!booking.canBeAccepted()) {
            throw new RuntimeException("Booking cannot be rejected in current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setCancellationReason(request.getReason());
        booking.setCancelledAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // Notify customer
        notificationService.notifyBookingRejected(booking);

        log.info("✅ Booking {} rejected", bookingId);
        return convertToDTO(booking);
    }

    @Transactional
    public BookingDTO completeBooking(String providerPhone, Long bookingId, CompleteBookingRequest request) {
        log.info("Provider {} completing booking {}", providerPhone, bookingId);

        Booking booking = getBookingForProvider(providerPhone, bookingId);

        if (!booking.canBeCompleted()) {
            throw new RuntimeException("Booking cannot be completed in current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setFinalPrice(request.getFinalPrice());
        booking.setCompletedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // Notify customer
        notificationService.notifyBookingCompleted(booking);

        log.info("✅ Booking {} completed with final price: {}", bookingId, request.getFinalPrice());
        return convertToDTO(booking);
    }

    @Transactional
    public BookingDTO cancelBooking(String userPhone, Long bookingId, BookingActionRequest request) {
        log.info("User {} cancelling booking {}", userPhone, bookingId);

        User user = userRepository.findByPhoneNumber(userPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));

        if (!booking.canBeCancelled()) {
            throw new RuntimeException("Booking cannot be cancelled in current status: " + booking.getStatus());
        }

        boolean isCustomerCancellation = booking.getCustomer().getId().equals(user.getId());

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        booking.setCancelledAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // Notify the other party
        notificationService.notifyBookingCancelled(booking, isCustomerCancellation);

        log.info("✅ Booking {} cancelled", bookingId);
        return convertToDTO(booking);
    }

    public BookingDTO getBookingById(String userPhone, Long bookingId) {
        User user = userRepository.findByPhoneNumber(userPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));

        return convertToDTO(booking);
    }

    private Booking getBookingForProvider(String providerPhone, Long bookingId) {
        User providerUser = userRepository.findByPhoneNumber(providerPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Provider provider = providerRepository.findByUserId(providerUser.getId())
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You don't have permission to modify this booking");
        }

        return booking;
    }

    private BookingDTO convertToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerPhone(booking.getCustomer().getPhoneNumber())
                .providerId(booking.getProvider().getId())
                .providerBusinessName(booking.getProvider().getBusinessName())
                .providerPhone(booking.getProvider().getUser().getPhoneNumber())
                .providerPhotoUrl(booking.getProvider().getPhotoUrl())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .serviceIcon(booking.getService().getIconEmoji())
                .scheduledDateTime(booking.getScheduledDateTime())
                .customerAddress(booking.getCustomerAddress())
                .customerLat(booking.getCustomerLat())
                .customerLng(booking.getCustomerLng())
                .description(booking.getDescription())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .quotedPrice(booking.getQuotedPrice())
                .finalPrice(booking.getFinalPrice())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .completedAt(booking.getCompletedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .canBeCancelled(booking.canBeCancelled())
                .canBeAccepted(booking.canBeAccepted())
                .canBeCompleted(booking.canBeCompleted())
                .build();
    }
}


