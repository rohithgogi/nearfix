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
    private final OtpRateLimiter rateLimiter;

     public void sendOtp(String phoneNumber){
         if (!rateLimiter.canSendOtp(phoneNumber)) {
             long cooldown = rateLimiter.getCooldownSeconds(phoneNumber);
             int remaining = rateLimiter.getRemainingAttempts(phoneNumber);

             if (cooldown > 0) {
                 throw new RuntimeException(
                         String.format("Please wait %d seconds before requesting another OTP", cooldown)
                 );
             } else {
                 throw new RuntimeException(
                         String.format("OTP limit exceeded. Try again later. (%d remaining today)", remaining)
                 );
             }
         }
         String code=String.format("%04d", new Random().nextInt(10000));

         Otp otp=Otp.builder()
                 .phoneNumber(phoneNumber)
                 .otpCode(code)
                 .expiresAt(LocalDateTime.now().plusMinutes(5))
                 .verified(false)
                 .build();

         otpRepository.save(otp);
         log.info("OTP generated for {}: {}", phoneNumber, code);

     }

     public boolean verifyOtp(String phoneNumber,String otpCode){
         return otpRepository.findTopByPhoneNumberOrderByExpiresAtDesc(phoneNumber)
                 .filter(o-> !o.getVerified() && o.getOtpCode().equals(otpCode) && o.getExpiresAt().isAfter(LocalDateTime.now()))
                 .map(otp -> {
                     otp.setVerified(true);
                     otp.setVerifiedAt(LocalDateTime.now());
                     otpRepository.save(otp);
                     log.info("OTP Verified successfully for {}", phoneNumber);
                     return true;
                 })
                 .orElseGet(()->{
                     log.warn("OTP verification failed for {}", phoneNumber);
                     return false;
                 });
     }
}
