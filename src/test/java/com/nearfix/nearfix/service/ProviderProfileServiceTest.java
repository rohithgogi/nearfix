package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.ProviderProfileDTO;
import com.nearfix.nearfix.dto.UpdateProviderProfileRequest;
import com.nearfix.nearfix.entity.AvailabilityStatus;
import com.nearfix.nearfix.entity.Provider;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.VerificationStatus;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderProfileServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProviderProfileService providerProfileService;

    private User user;
    private Provider provider;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPhoneNumber("9999999999");

        provider = new Provider();
        provider.setId(10L);
        provider.setUser(user);
        provider.setVerificationStatus(VerificationStatus.PENDING);
        provider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        provider.setVerified(false);
        provider.setProfileCompleted(false);
        provider.setExperienceYears(0);
    }

    @Test
    void getProfile_returnsExistingProfile() {
        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));

        ProviderProfileDTO result = providerProfileService.getProfile(user.getPhoneNumber());

        assertNotNull(result);
        assertEquals(provider.getId(), result.getId());
        assertEquals(user.getPhoneNumber(), result.getPhoneNumber());
        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void getProfile_createsProviderWhenMissing() {
        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider created = invocation.getArgument(0);
            created.setId(99L);
            return created;
        });

        ProviderProfileDTO result = providerProfileService.getProfile(user.getPhoneNumber());

        assertEquals(99L, result.getId());
        assertEquals("OFFLINE", result.getAvailabilityStatus());
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void updateProfile_updatesFieldsAndReturnsDto() {
        UpdateProviderProfileRequest request = new UpdateProviderProfileRequest();
        request.setBusinessName("FixFast");
        request.setAddress("Street 1");
        request.setCity("Delhi");
        request.setPincode("110001");
        request.setLatitude(new BigDecimal("28.6139"));
        request.setLongitude(new BigDecimal("77.2090"));
        request.setBio("Experienced technician");
        request.setWorkingHours("{\"mon\":\"9-6\"}");
        request.setExperienceYears(4);

        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderProfileDTO result = providerProfileService.updateProfile(user.getPhoneNumber(), request);

        assertEquals("FixFast", result.getBusinessName());
        assertEquals("Delhi", result.getCity());
        assertEquals(4, result.getExperienceYears());
        assertEquals(80, result.getProfileCompletionPercentage());
    }

    @Test
    void updateAvailability_setsStatus() {
        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderProfileDTO result = providerProfileService.updateAvailability(
                user.getPhoneNumber(),
                AvailabilityStatus.AVAILABLE
        );

        assertEquals("AVAILABLE", result.getAvailabilityStatus());
    }

    @Test
    void uploadPhoto_replacesOldPhotoAndStoresNewOne() {
        provider.setPhotoUrl("old-photo-url");
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", "photo".getBytes()
        );

        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(fileStorageService.storeFile(file, "photos")).thenReturn("new-photo-url");
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderProfileDTO result = providerProfileService.uploadPhoto(user.getPhoneNumber(), file);

        assertEquals("new-photo-url", result.getPhotoUrl());
        verify(fileStorageService).deleteFile("old-photo-url");
        verify(fileStorageService).storeFile(file, "photos");
    }

    @Test
    void uploadDocument_replacesOldDocumentAndStoresNewOne() {
        provider.setAadharUrl("old-document-url");
        MockMultipartFile file = new MockMultipartFile(
                "file", "aadhar.pdf", "application/pdf", "doc".getBytes()
        );

        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(fileStorageService.storeFile(file, "documents")).thenReturn("new-document-url");
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderProfileDTO result = providerProfileService.uploadDocument(user.getPhoneNumber(), file);

        assertEquals("new-document-url", result.getAadharUrl());
        verify(fileStorageService).deleteFile("old-document-url");
        verify(fileStorageService).storeFile(file, "documents");
    }

    @Test
    void getProfile_throwsWhenUserMissing() {
        when(userRepository.findByPhoneNumber("0000000000")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> providerProfileService.getProfile("0000000000")
        );

        assertEquals("User not found", ex.getMessage());
    }
}
