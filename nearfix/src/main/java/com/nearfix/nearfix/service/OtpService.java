package com.nearfix.nearfix.service;

import com.nearfix.nearfix.entity.Otp;
import com.nearfix.nearfix.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Slf4j
public class OtpService {
    private final OtpRepository otpRepository;
    private final SnsService snsService;
     public void sendOtp(String phoneNumber){
         String code=String.format("%04d", new Random().nextInt(10000));

         Otp otp=Otp.builder()
                 .phoneNumber(phoneNumber)
                 .otpCode(code)
                 .expiresAt(LocalDateTime.now().plusMinutes(5))
                 .build();

         otpRepository.save(otp);
         log.info("OTP generated for {}: {}", phoneNumber, code);
         // Send OTP via SMS
         try {
             snsService.sendOtp(phoneNumber, code);
             log.info("OTP sent successfully to {}", phoneNumber);
         } catch (Exception e) {
             log.error("Failed to send OTP to {}: {}", phoneNumber, e.getMessage());
             // Note: We don't throw exception here so OTP is still saved in DB for testing
             // In production, you might want to throw exception and rollback
         }
     }

     public boolean verifyOtp(String phoneNumber,String otpCode){
         return otpRepository.findTopByPhoneNumberOrderByExpiresAtDesc(phoneNumber)
                 .filter(o->o.getOtpCode().equals(otpCode) && o.getExpiresAt().isAfter(LocalDateTime.now()))
                 .isPresent();
     }
}
