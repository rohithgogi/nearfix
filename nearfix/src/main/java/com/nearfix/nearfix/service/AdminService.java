package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.*;
import com.nearfix.nearfix.entity.*;
import com.nearfix.nearfix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderServiceRepository providerServiceRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Get dashboard statistics
     */
    public AdminDashboardStatsDTO getDashboardStats() {
        log.info("Fetching admin dashboard statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);

        // User Statistics
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long totalProviders = userRepository.countByRole(UserRole.PROVIDER);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(monthStart);

        // Provider Statistics
        long pendingVerifications = providerRepository.countByVerificationStatus(VerificationStatus.PENDING);
        long verifiedProviders = providerRepository.countByVerified(true);
        long rejectedProviders = providerRepository.countByVerificationStatus(VerificationStatus.REJECTED);

        // Booking Statistics
        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        long bookingsThisMonth = bookingRepository.countByCreatedAtAfter(monthStart);

        // Revenue Statistics
        BigDecimal totalRevenue = calculateTotalRevenue();
        BigDecimal revenueThisMonth = calculateRevenueForPeriod(monthStart, now);
        BigDecimal averageBookingValue = completedBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Service Statistics
        long totalServices = serviceRepository.count();
        long activeProviderServices = providerServiceRepository.countByAvailableTrue();

        // Growth Metrics
        long lastMonthUsers = userRepository.countByCreatedAtBetween(lastMonthStart, monthStart);
        double userGrowthRate = calculateGrowthRate(lastMonthUsers, newUsersThisMonth);

        long lastMonthBookings = bookingRepository.countByCreatedAtBetween(lastMonthStart, monthStart);
        double bookingGrowthRate = calculateGrowthRate(lastMonthBookings, bookingsThisMonth);

        BigDecimal lastMonthRevenue = calculateRevenueForPeriod(lastMonthStart, monthStart);
        double revenueGrowthRate = calculateRevenueGrowthRate(lastMonthRevenue, revenueThisMonth);

        // Recent Activities
        List<AdminDashboardStatsDTO.RecentActivityDTO> recentActivities = getRecentActivities();

        return AdminDashboardStatsDTO.builder()
                .totalUsers((int) totalUsers)
                .totalCustomers((int) totalCustomers)
                .totalProviders((int) totalProviders)
                .newUsersThisMonth((int) newUsersThisMonth)
                .pendingVerifications((int) pendingVerifications)
                .verifiedProviders((int) verifiedProviders)
                .rejectedProviders((int) rejectedProviders)
                .totalBookings((int) totalBookings)
                .pendingBookings((int) pendingBookings)
                .completedBookings((int) completedBookings)
                .cancelledBookings((int) cancelledBookings)
                .bookingsThisMonth((int) bookingsThisMonth)
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .averageBookingValue(averageBookingValue)
                .totalServices((int) totalServices)
                .activeProviderServices((int) activeProviderServices)
                .recentActivities(recentActivities)
                .userGrowthRate(userGrowthRate)
                .bookingGrowthRate(bookingGrowthRate)
                .revenueGrowthRate(revenueGrowthRate)
                .build();
    }

    /**
     * Get pending provider verifications
     */
    public Page<ProviderVerificationDTO> getPendingVerifications(int page, int size) {
        log.info("Fetching pending verifications - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Provider> providers = providerRepository.findByVerificationStatusIn(
                List.of(VerificationStatus.PENDING, VerificationStatus.UNDER_REVIEW),
                pageable
        );

        return providers.map(this::convertToVerificationDTO);
    }

    /**
     * Verify a provider
     */
    @Transactional
    public ProviderVerificationDTO verifyProvider(Long providerId, String adminNotes) {
        log.info("Verifying provider: {}", providerId);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (provider.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new RuntimeException("Provider is already verified");
        }

        if (!provider.getProfileCompleted()) {
            throw new RuntimeException("Provider profile is incomplete");
        }

        provider.setVerificationStatus(VerificationStatus.VERIFIED);
        provider.setVerified(true);
        provider = providerRepository.save(provider);

        log.info("✅ Provider {} verified successfully", providerId);

        // TODO: Send notification to provider
        // notificationService.notifyProviderVerified(provider);

        return convertToVerificationDTO(provider);
    }

    /**
     * Reject a provider verification
     */
    @Transactional
    public ProviderVerificationDTO rejectProvider(Long providerId, String reason) {
        log.info("Rejecting provider: {} with reason: {}", providerId, reason);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        provider.setVerificationStatus(VerificationStatus.REJECTED);
        provider.setVerified(false);
        provider = providerRepository.save(provider);

        log.info("❌ Provider {} rejected", providerId);

        // TODO: Send notification to provider with reason
        // notificationService.notifyProviderRejected(provider, reason);

        return convertToVerificationDTO(provider);
    }

    /**
     * Get all users with pagination
     */
    public Page<UserManagementDTO> getAllUsers(int page, int size, String role, String search) {
        log.info("Fetching users - page: {}, size: {}, role: {}, search: {}", page, size, role, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> users;

        if (role != null && !role.isEmpty()) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            users = userRepository.findByRole(userRole, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::convertToUserManagementDTO);
    }

    /**
     * Deactivate a user
     */
    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // You might want to add an 'active' field to User entity
        // For now, we'll just log it
        log.warn("User deactivation not fully implemented - userId: {}", userId);

        // TODO: Implement user deactivation logic
        // user.setActive(false);
        // userRepository.save(user);
    }

    // Helper Methods

    private BigDecimal calculateTotalRevenue() {
        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);
        return completedBookings.stream()
                .filter(b -> b.getFinalPrice() != null)
                .map(Booking::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Booking> bookings = bookingRepository.findByStatusAndCompletedAtBetween(
                BookingStatus.COMPLETED, start, end);
        return bookings.stream()
                .filter(b -> b.getFinalPrice() != null)
                .map(Booking::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateGrowthRate(long previousPeriod, long currentPeriod) {
        if (previousPeriod == 0) return currentPeriod > 0 ? 100.0 : 0.0;
        return ((double) (currentPeriod - previousPeriod) / previousPeriod) * 100;
    }

    private double calculateRevenueGrowthRate(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private List<AdminDashboardStatsDTO.RecentActivityDTO> getRecentActivities() {
        List<AdminDashboardStatsDTO.RecentActivityDTO> activities = new ArrayList<>();

        // Recent bookings
        List<Booking> recentBookings = bookingRepository.findTop5ByOrderByCreatedAtDesc();
        for (Booking booking : recentBookings) {
            activities.add(AdminDashboardStatsDTO.RecentActivityDTO.builder()
                    .type("BOOKING")
                    .description("New booking: " + booking.getService().getName())
                    .timestamp(booking.getCreatedAt())
                    .userPhone(booking.getCustomer().getPhoneNumber())
                    .entityId(booking.getId())
                    .build());
        }

        // Recent provider registrations
        List<Provider> recentProviders = providerRepository.findTop5ByOrderByCreatedAtDesc();
        for (Provider provider : recentProviders) {
            activities.add(AdminDashboardStatsDTO.RecentActivityDTO.builder()
                    .type("REGISTRATION")
                    .description("New provider registration: " +
                            (provider.getBusinessName() != null ? provider.getBusinessName() : "Unnamed"))
                    .timestamp(provider.getCreatedAt())
                    .userPhone(provider.getUser().getPhoneNumber())
                    .entityId(provider.getId())
                    .build());
        }

        // Sort by timestamp
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities.stream().limit(10).collect(Collectors.toList());
    }

    private ProviderVerificationDTO convertToVerificationDTO(Provider provider) {
        List<ProviderService> services = providerServiceRepository.findByProviderId(provider.getId());

        List<ProviderVerificationDTO.ServiceOfferedDTO> servicesOffered = services.stream()
                .map(ps -> ProviderVerificationDTO.ServiceOfferedDTO.builder()
                        .serviceId(ps.getService().getId())
                        .serviceName(ps.getService().getName())
                        .serviceIcon(ps.getService().getIconEmoji())
                        .basePrice(ps.getBasePrice())
                        .experienceYears(ps.getExperienceYears())
                        .build())
                .collect(Collectors.toList());

        return ProviderVerificationDTO.builder()
                .providerId(provider.getId())
                .phoneNumber(provider.getUser().getPhoneNumber())
                .businessName(provider.getBusinessName())
                .address(provider.getAddress())
                .city(provider.getCity())
                .pincode(provider.getPincode())
                .latitude(provider.getLatitude())
                .longitude(provider.getLongitude())
                .photoUrl(provider.getPhotoUrl())
                .aadharUrl(provider.getAadharUrl())
                .bio(provider.getBio())
                .workingHours(provider.getWorkingHours())
                .experienceYears(provider.getExperienceYears())
                .verificationStatus(provider.getVerificationStatus())
                .profileCompletionPercentage(provider.getProfileCompletionPercentage())
                .profileCompleted(provider.getProfileCompleted())
                .services(servicesOffered)
                .createdAt(provider.getCreatedAt())
                .updatedAt(provider.getUpdatedAt())
                .totalBookings(provider.getTotalBookings())
                .rating(provider.getRating())
                .totalReviews(provider.getTotalReviews())
                .build();
    }

    private UserManagementDTO convertToUserManagementDTO(User user) {
        UserManagementDTO dto = UserManagementDTO.builder()
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .city(user.getCity())
                .phoneVerified(user.isPhoneVerified())
                .active(true) // Default to true for now
                .build();

        // Add provider-specific info
        if (user.getRole() == UserRole.PROVIDER) {
            Provider provider = providerRepository.findByUserId(user.getId()).orElse(null);
            if (provider != null) {
                dto.setBusinessName(provider.getBusinessName());
                dto.setProfileCompleted(provider.getProfileCompleted());
                dto.setVerificationStatus(provider.getVerificationStatus().name());
                dto.setTotalBookings(provider.getTotalBookings());
            }
        }

        // Add booking stats
        if (user.getRole() == UserRole.CUSTOMER) {
            Long totalBookings = bookingRepository.countByCustomerId(user.getId());
            Long completedBookings = bookingRepository.countByCustomerIdAndStatus(
                    user.getId(), BookingStatus.COMPLETED);
            dto.setTotalBookings(totalBookings.intValue());
            dto.setCompletedBookings(completedBookings.intValue());
        }

        return dto;
    }
}