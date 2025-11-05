package com.nearfix.nearfix.service;

import com.nearfix.nearfix.entity.Otp;
import com.nearfix.nearfix.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class OtpService {
    private final OtpRepository otpRepository;
     public void sendOtp(String phoneNumber){
         String code=String.format("%04d", new Random().nextInt(10000));

         Otp otp=Otp.builder()
                 .phoneNumber(phoneNumber)
                 .otpCode(code)
                 .expiresAt(LocalDateTime.now().plusMinutes(5))
                 .build();

         otpRepository.save(otp);
     }

     public boolean verifyOtp(String phoneNumber,String otpCode){
         return otpRepository.findTopByPhoneNumberOrderByExpiresAtDesc(phoneNumber)
                 .filter(o->o.getOtpCode().equals(otpCode) && o.getExpiresAt().isAfter(LocalDateTime.now()))
                 .isPresent();
     }
}
