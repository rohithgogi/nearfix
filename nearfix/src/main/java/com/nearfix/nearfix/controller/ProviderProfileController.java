package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.ProviderProfileDTO;
import com.nearfix.nearfix.dto.UpdateAvailabilityRequest;
import com.nearfix.nearfix.dto.UpdateProviderProfileRequest;
import com.nearfix.nearfix.security.JwtTokenProvider;
import com.nearfix.nearfix.service.ProviderProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/provider/profile")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5175")
public class ProviderProfileController {

    private final ProviderProfileService providerProfileService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ProviderProfileDTO> getProfile(HttpServletRequest request) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            ProviderProfileDTO profile = providerProfileService.getProfile(phoneNumber);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching profile: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<ProviderProfileDTO> updateProfile(
            HttpServletRequest request,
            @RequestBody @Valid UpdateProviderProfileRequest profileRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            ProviderProfileDTO profile = providerProfileService.updateProfile(phoneNumber, profileRequest);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // ✅ NEW ENDPOINT: Update Availability Status
    @PutMapping("/availability")
    public ResponseEntity<ProviderProfileDTO> updateAvailability(
            HttpServletRequest request,
            @RequestBody UpdateAvailabilityRequest availabilityRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            log.info("Updating availability for provider: {} to {}",
                    phoneNumber, availabilityRequest.getAvailabilityStatus());

            ProviderProfileDTO profile = providerProfileService.updateAvailability(
                    phoneNumber,
                    availabilityRequest.getAvailabilityStatus()
            );

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error updating availability: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/photo")
    public ResponseEntity<ProviderProfileDTO> uploadPhoto(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);

            if (file.isEmpty()) {
                throw new RuntimeException("Please select a file to upload");
            }

            ProviderProfileDTO profile = providerProfileService.uploadPhoto(phoneNumber, file);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error uploading photo: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/document")
    public ResponseEntity<ProviderProfileDTO> uploadDocument(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);

            if (file.isEmpty()) {
                throw new RuntimeException("Please select a file to upload");
            }

            ProviderProfileDTO profile = providerProfileService.uploadDocument(phoneNumber, file);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Map<String, String>> deletePhoto(HttpServletRequest request) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            // Implementation for deleting photo
            return ResponseEntity.ok(Map.of("message", "Photo deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting photo: {}", e.getMessage());
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