package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.AuthResponse;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.UserRole;
import com.nearfix.nearfix.repository.UserRepository;
import com.nearfix.nearfix.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // Find user by phone number
    public User findUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    // Register new user with selected role
    public User registerUser(String phoneNumber, UserRole role) {
        User newUser = new User();
        newUser.setPhoneNumber(phoneNumber);
        newUser.setRole(role);
        newUser.setPhoneVerified(true); // OTP was verified
        return userRepository.save(newUser);
    }

    public AuthResponse registerUserWithRole(String phoneNumber, UserRole role) {
        User newUser = registerUser(phoneNumber, role);
        String token = generateToken(newUser);
        return new AuthResponse(token, newUser.getPhoneNumber(), newUser.getRole().name());
    }    // Old method (kept for backwards compatibility, but not used anymore)
    public User loginOrRegister(String phoneNumber){
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(()->{
                    User newUser = new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setRole(UserRole.CUSTOMER);
                    return userRepository.save(newUser);
                });
    }

    // Generate JWT token
    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getPhoneNumber(), user.getRole().name());
    }
}