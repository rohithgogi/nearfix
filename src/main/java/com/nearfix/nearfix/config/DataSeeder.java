package com.nearfix.nearfix.config;

import com.nearfix.nearfix.entity.*;
import com.nearfix.nearfix.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ProviderServiceRepository providerServiceRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            try {
                log.info("=== Starting Data Seeding ===");

                // Seed services
                if (serviceRepository.count() == 0) {
                    log.info("Seeding services...");
                    seedServices();
                    log.info("✅ Services seeded successfully");
                } else {
                    log.info("⏭️ Services already exist, skipping");
                }

                // Seed providers
                if (providerRepository.count() == 0) {
                    log.info("Seeding providers...");
                    seedProviders();
                    log.info("✅ Providers seeded successfully");
                } else {
                    log.info("⏭️ Providers already exist, skipping");
                }

                log.info("=== Data Seeding Complete ===");
            } catch (Exception e) {
                log.error("❌ Error seeding data", e);
            }
        };
    }

    private void seedServices() {
        String[][] servicesData = {
                {"Plumbing", "Home Repair", "Professional plumbing services", "🔧"},
                {"Electrical", "Home Repair", "Electrical maintenance and repair", "⚡"},
                {"Carpentry", "Home Repair", "Wooden furniture and repairs", "🪵"},
                {"Cleaning", "Cleaning", "Professional home cleaning", "🧹"},
                {"Painting", "Home Repair", "Interior and exterior painting", "🎨"},
                {"AC Repair", "Appliances", "Air conditioning repair", "❄️"},
                {"Mobile Repair", "Electronics", "Mobile phone repair services", "📱"},
        };

        for (String[] data : servicesData) {
            Service service = new Service();
            service.setName(data[0]);
            service.setCategory(data[1]);
            service.setDescription(data[2]);
            service.setIconEmoji(data[3]);
            service.setActive(true);
            service.setCreatedAt(LocalDateTime.now());
            service.setUpdatedAt(LocalDateTime.now());
            serviceRepository.save(service);
            log.info("✅ Added service: {}", data[0]);
        }
    }

    private void seedProviders() {
        // Create test users and providers
        String[][] providersData = {
                {"9876543210", "ABC Plumbing", "Delhi", "110001", "28.7041", "77.1025", "🔧", String.valueOf(1)},
                {"9876543211", "XYZ Electrical", "Mumbai", "400001", "19.0760", "72.8777", "⚡", String.valueOf(2)},
                {"9876543212", "Quick Cleaning", "Bangalore", "560001", "12.9716", "77.5946", "🧹", String.valueOf(4)},
        };

        for (String[] data : providersData) {
            try {
                String phoneNumber = data[0];
                String businessName = data[1];
                String city = data[2];
                String pincode = data[3];
                BigDecimal latitude = new BigDecimal(data[4]);
                BigDecimal longitude = new BigDecimal(data[5]);
                Long serviceId = Long.parseLong(data[7]);

                // Create or get user
                User user = userRepository.findByPhoneNumber(phoneNumber)
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setPhoneNumber(phoneNumber);
                            newUser.setRole(UserRole.PROVIDER);
                            newUser.setPhoneVerified(true);
                            newUser.setCity(city);
                            newUser.setCreatedAt(LocalDateTime.now());
                            newUser.setUpdatedAt(LocalDateTime.now());
                            return userRepository.save(newUser);
                        });

                // Create provider
                Provider provider = new Provider();
                provider.setUser(user);
                provider.setBusinessName(businessName);
                provider.setAddress("123 " + businessName + " Street");
                provider.setCity(city);
                provider.setPincode(pincode);
                provider.setLatitude(latitude);
                provider.setLongitude(longitude);
                provider.setBio("Expert " + businessName + " with 10+ years experience");
                provider.setExperienceYears(10);
                provider.setVerificationStatus(VerificationStatus.VERIFIED);
                provider.setVerified(true);
                provider.setProfileCompleted(true);
                provider.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
                provider.setRating(new BigDecimal("4.5"));
                provider.setTotalReviews(15);
                provider.setTotalBookings(30);
                provider.setCreatedAt(LocalDateTime.now());
                provider.setUpdatedAt(LocalDateTime.now());

                Provider savedProvider = providerRepository.save(provider);
                log.info("✅ Added provider: {} (ID: {})", businessName, savedProvider.getId());

                // Add service to provider
                Service service = serviceRepository.findById(serviceId).orElse(null);
                if (service != null) {
                    ProviderService ps = new ProviderService();
                    ps.setProvider(savedProvider);
                    ps.setService(service);
                    ps.setBasePrice(new BigDecimal("500.00"));
                    ps.setExperienceYears(10);
                    ps.setDescription("Professional " + service.getName() + " service");
                    ps.setAvailable(true);
                    ps.setCreatedAt(LocalDateTime.now());
                    ps.setUpdatedAt(LocalDateTime.now());
                    providerServiceRepository.save(ps);
                    log.info("✅ Linked service {} to provider {}", service.getName(), businessName);
                }
            } catch (Exception e) {
                log.error("Error seeding provider", e);
            }
        }
    }
}