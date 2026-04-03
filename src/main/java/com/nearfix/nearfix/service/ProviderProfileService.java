package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.ProviderProfileDTO;
import com.nearfix.nearfix.dto.UpdateProviderProfileRequest;
import com.nearfix.nearfix.entity.AvailabilityStatus;
import com.nearfix.nearfix.entity.Provider;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.VerificationStatus;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderProfileService {

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ProviderProfileDTO getProfile(String phoneNumber) {
        log.info("Fetching profile for provider: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get or create provider profile
        Provider provider = providerRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Provider profile not found for user {}. Creating new profile...", phoneNumber);
                    Provider newProvider = new Provider();
                    newProvider.setUser(user);
                    newProvider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
                    newProvider.setVerified(false);
                    newProvider.setVerificationStatus(VerificationStatus.PENDING);
                    newProvider.setProfileCompleted(false);
                    newProvider.setExperienceYears(0); // Initialize to 0
                    Provider saved = providerRepository.save(newProvider);
                    log.info("New provider profile created with ID: {}", saved.getId());
                    return saved;
                });

        return convertToDTO(provider);
    }

    @Transactional
    @CacheEvict(value = "providerDetail", key = "#result.id", condition = "#result != null")

    public ProviderProfileDTO updateProfile(String phoneNumber, UpdateProviderProfileRequest request) {
        log.info("Updating profile for provider: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get or create provider profile
        Provider provider = providerRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Provider profile not found for user {}. Creating new profile...", phoneNumber);
                    Provider newProvider = new Provider();
                    newProvider.setUser(user);
                    newProvider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
                    newProvider.setVerified(false);
                    newProvider.setVerificationStatus(VerificationStatus.PENDING);
                    newProvider.setProfileCompleted(false);
                    newProvider.setExperienceYears(0); // Initialize to 0
                    return providerRepository.save(newProvider);
                });

        // Update fields
        provider.setBusinessName(request.getBusinessName());
        provider.setAddress(request.getAddress());
        provider.setCity(request.getCity());
        provider.setPincode(request.getPincode());
        provider.setLatitude(request.getLatitude());
        provider.setLongitude(request.getLongitude());
        provider.setBio(request.getBio());
        provider.setWorkingHours(request.getWorkingHours());
        provider.setExperienceYears(request.getExperienceYears());

        // Update profile completion status
        updateProfileCompletion(provider);

        provider = providerRepository.save(provider);
        log.info("Profile updated successfully for: {}", phoneNumber);

        return convertToDTO(provider);
    }

    // ✅ NEW METHOD: Update Availability Status
    @Transactional
    public ProviderProfileDTO updateAvailability(String phoneNumber, AvailabilityStatus status) {
        log.info("Updating availability for provider: {} to {}", phoneNumber, status);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Provider provider = providerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        // Update availability
        provider.setAvailabilityStatus(status);
        provider = providerRepository.save(provider);

        log.info("✅ Availability updated successfully: {} is now {}",
                phoneNumber, status);

        return convertToDTO(provider);
    }

    @Transactional
    @CacheEvict(value = "providerDetail", key = "#result.id", condition = "#result != null")
    public ProviderProfileDTO uploadPhoto(String phoneNumber, MultipartFile file) {
        log.info("Uploading photo for provider: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get or create provider profile
        Provider provider = providerRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Provider profile not found for user {}. Creating new profile...", phoneNumber);
                    Provider newProvider = new Provider();
                    newProvider.setUser(user);
                    newProvider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
                    newProvider.setVerified(false);
                    newProvider.setVerificationStatus(VerificationStatus.PENDING);
                    newProvider.setProfileCompleted(false);
                    newProvider.setExperienceYears(0); // Initialize to 0
                    return providerRepository.save(newProvider);
                });

        // Delete old photo if exists
        if (provider.getPhotoUrl() != null) {
            fileStorageService.deleteFile(provider.getPhotoUrl());
        }

        // Store new photo
        String photoUrl = fileStorageService.storeFile(file, "photos");
        provider.setPhotoUrl(photoUrl);

        // Update profile completion status
        updateProfileCompletion(provider);

        provider = providerRepository.save(provider);
        log.info("Photo uploaded successfully: {}", photoUrl);

        return convertToDTO(provider);
    }

    @Transactional
    @CacheEvict(value = "providerDetail", key = "#result.id", condition = "#result != null")
    public ProviderProfileDTO uploadDocument(String phoneNumber, MultipartFile file) {
        log.info("Uploading document for provider: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get or create provider profile
        Provider provider = providerRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Provider profile not found for user {}. Creating new profile...", phoneNumber);
                    Provider newProvider = new Provider();
                    newProvider.setUser(user);
                    newProvider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
                    newProvider.setVerified(false);
                    newProvider.setVerificationStatus(VerificationStatus.PENDING);
                    newProvider.setProfileCompleted(false);
                    return providerRepository.save(newProvider);
                });

        // Delete old document if exists
        if (provider.getAadharUrl() != null) {
            fileStorageService.deleteFile(provider.getAadharUrl());
        }

        // Store new document
        String documentUrl = fileStorageService.storeFile(file, "documents");
        provider.setAadharUrl(documentUrl);

        // Update profile completion status
        updateProfileCompletion(provider);

        provider = providerRepository.save(provider);
        log.info("Document uploaded successfully: {}", documentUrl);

        return convertToDTO(provider);
    }

    private void updateProfileCompletion(Provider provider) {
        int completionPercentage = provider.getProfileCompletionPercentage();
        provider.setProfileCompleted(completionPercentage == 100);
        log.info("Profile completion: {}%", completionPercentage);
    }

    private ProviderProfileDTO convertToDTO(Provider provider) {
        ProviderProfileDTO dto = new ProviderProfileDTO();
        dto.setId(provider.getId());
        dto.setPhoneNumber(provider.getUser().getPhoneNumber());
        dto.setBusinessName(provider.getBusinessName());
        dto.setAddress(provider.getAddress());
        dto.setCity(provider.getCity());
        dto.setPincode(provider.getPincode());
        dto.setLatitude(provider.getLatitude());
        dto.setLongitude(provider.getLongitude());
        dto.setPhotoUrl(provider.getPhotoUrl());
        dto.setAadharUrl(provider.getAadharUrl());
        dto.setWorkingHours(provider.getWorkingHours());
        dto.setBio(provider.getBio());
        dto.setVerificationStatus(provider.getVerificationStatus());
        dto.setProfileCompleted(provider.getProfileCompleted());
        dto.setProfileCompletionPercentage(provider.getProfileCompletionPercentage());
        dto.setExperienceYears(provider.getExperienceYears());
        dto.setAvailabilityStatus(provider.getAvailabilityStatus().name());
        return dto;
    }
}