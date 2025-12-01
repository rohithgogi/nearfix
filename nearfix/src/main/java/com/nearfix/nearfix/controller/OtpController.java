package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.AuthResponse;
import com.nearfix.nearfix.dto.OtpResponseVerify;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.UserRole;
import com.nearfix.nearfix.service.AuthService;
import com.nearfix.nearfix.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173") // Add this
public class OtpController {
    private final OtpService otpService;
    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber){
        try {
            log.info("Sending OTP to: {}", phoneNumber);
            otpService.sendOtp(phoneNumber);
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP sent successfully");
            response.put("phoneNumber", phoneNumber);
            log.info("OTP send response prepared for: {}", phoneNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending OTP to {}: {}", phoneNumber, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber,
                                       @RequestParam String otpCode){
        try {
            log.info("Verifying OTP for: {} with code: {}", phoneNumber, otpCode);
            boolean isValid = otpService.verifyOtp(phoneNumber, otpCode);

            if (!isValid) {
                log.warn("Invalid or expired OTP for: {}", phoneNumber);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid or Expired OTP");
                return ResponseEntity.badRequest().body(error);
            }

            log.info("OTP verified successfully for: {}", phoneNumber);

            // Check if user exists
            User existingUser = authService.findUserByPhone(phoneNumber);
            if (existingUser != null) {
                log.info("Existing user found: {}, role: {}", phoneNumber, existingUser.getRole());
                String token = authService.generateToken(existingUser);
                AuthResponse response = new AuthResponse(
                        token,
                        existingUser.getPhoneNumber(),
                        existingUser.getRole().name()
                );
                return ResponseEntity.ok(response);
            } else {
                log.info("New user detected: {}", phoneNumber);
                OtpResponseVerify response = new OtpResponseVerify(
                        true,  // newUser = true
                        phoneNumber,
                        null,
                        null
                );
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error verifying OTP for {}: {}", phoneNumber, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error verifying OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register-with-role")
    public ResponseEntity<?> registerWithRole(@RequestParam String phoneNumber,
                                              @RequestParam String role){
        try {
            log.info("Registering user {} with role: {}", phoneNumber, role);

            // Validate role
            UserRole userRole;
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
                if(userRole == UserRole.ADMIN) {
                    log.warn("Attempt to register as ADMIN by: {}", phoneNumber);
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Cannot register as ADMIN");
                    return ResponseEntity.badRequest().body(error);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role provided: {} for user: {}", role, phoneNumber);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid role. Use CUSTOMER or PROVIDER");
                return ResponseEntity.badRequest().body(error);
            }

            // Register new user
            User newUser = authService.registerUser(phoneNumber, userRole);
            String token = authService.generateToken(newUser);

            log.info("User registered successfully: {}, role: {}", phoneNumber, userRole);

            AuthResponse response = new AuthResponse(
                    token,
                    newUser.getPhoneNumber(),
                    newUser.getRole().name()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error registering user {}: {}", phoneNumber, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error registering user: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}