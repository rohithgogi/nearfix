package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.AuthResponse;
import com.nearfix.nearfix.dto.OtpResponseVerify;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.UserRole;
import com.nearfix.nearfix.service.AuthService;
import com.nearfix.nearfix.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Add this
public class OtpController {
    private final OtpService otpService;
    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber){
        try {
            otpService.sendOtp(phoneNumber);
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send OTP");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber,
                                       @RequestParam String otpCode){
        try {
            boolean isValid = otpService.verifyOtp(phoneNumber, otpCode);
            if (!isValid) {
                return ResponseEntity.badRequest().body("Invalid or Expired OTP");
            }

            User existingUser = authService.findUserByPhone(phoneNumber);
            if (existingUser != null) {
                String token = authService.generateToken(existingUser);
                return ResponseEntity.ok(new AuthResponse(token,
                        existingUser.getPhoneNumber(),
                        existingUser.getRole().name()));
            } else {
                return ResponseEntity.ok(new OtpResponseVerify(true,
                        phoneNumber,
                        null,
                        null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying OTP");
        }
    }

    @PostMapping("/register-with-role")
    public ResponseEntity<?> registerWithRole(@RequestParam String phoneNumber,
                                              @RequestParam String role){
        try {
            // Validate role
            UserRole userRole;
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
                if(userRole == UserRole.ADMIN) {
                    return ResponseEntity.badRequest().body("Cannot register as ADMIN");
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid role. Use CUSTOMER or PROVIDER");
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
            return ResponseEntity.badRequest().body("Error registering user");
        }
    }
}