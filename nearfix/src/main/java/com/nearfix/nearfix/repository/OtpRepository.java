package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp,Long> {
    Optional<Otp> findTopByPhoneNumberOrderByExpiresAtDesc(String phoneNumber);
}
