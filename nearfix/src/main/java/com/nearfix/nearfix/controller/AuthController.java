package com.nearfix.nearfix.controller;


import com.nearfix.nearfix.dto.AuthResponse;
import com.nearfix.nearfix.dto.OtpResponseVerify;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.UserRole;
import com.nearfix.nearfix.service.AuthService;
import com.nearfix.nearfix.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    /**
     * Send OTP to user's phone number
     * SNS notification is commented out for testing
     */
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp(@RequestParam String phoneNumber) {
        try {
            otpService.sendOtp(phoneNumber);
            return ResponseEntity.ok("OTP sent successfully to " + phoneNumber);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send OTP: " + e.getMessage());
        }
    }

    /**
     * Verify OTP code
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String phoneNumber,
            @RequestParam String otpCode) {
        try {
            // Verify OTP
            boolean isValid = otpService.verifyOtp(phoneNumber, otpCode);

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or Expired OTP");
            }

            // Check if user exists
            User existingUser = authService.findUserByPhone(phoneNumber);

            if (existingUser != null) {
                // Existing user - return token immediately
                String token = authService.generateToken(existingUser);
                return ResponseEntity.ok(new AuthResponse(
                        token,
                        existingUser.getPhoneNumber(),
                        existingUser.getRole().name()
                ));
            } else {
                // New user - ask for role selection
                return ResponseEntity.ok(new OtpResponseVerify(
                        true,  // isNewUser
                        phoneNumber,
                        null,  // token (null for new user)
                        null   // role (null for new user)
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OTP verification failed: " + e.getMessage());
        }
    }

    /**
     * Register user with role after OTP verification
     */
    @PostMapping("/register-with-role")
    public ResponseEntity<?> registerWithRole(
            @RequestParam String phoneNumber,
            @RequestParam String role) {
        try {
            // Validate role
            UserRole userRole;
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
                // Don't allow ADMIN registration via public endpoint
                if (userRole == UserRole.ADMIN) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Cannot register as ADMIN");
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid role. Use CUSTOMER or PROVIDER");
            }

            // Register new user
            User newUser = authService.registerUser(phoneNumber, userRole);
            String token = authService.generateToken(newUser);

            return ResponseEntity.ok(new AuthResponse(
                    token,
                    newUser.getPhoneNumber(),
                    newUser.getRole().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }
}