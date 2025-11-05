package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.service.AuthService;
import com.nearfix.nearfix.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;
    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber){
        otpService.sendOtp(phoneNumber);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber,
                                       @RequestParam String otpCode){
        boolean isValid=otpService.verifyOtp(phoneNumber,otpCode);
        if(!isValid){
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
        User user = authService.loginOrRegister(phoneNumber);
        return ResponseEntity.ok("OTP verified");
    }
}
