package com.nearfix.nearfix.service;

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

    public User findUserByPhone(String phoneNumber)
    {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }
    public User registerUser(String phoneNumber,UserRole role){
        User newUser=new User();
        newUser.setPhoneNumber(phoneNumber);
        newUser.setRole(role);
        newUser.setPhoneVerified(true);
        return userRepository.save(newUser);
    }
    public User loginOrRegister(String phoneNumber){
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(()->{
                    User newUser=new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setRole(UserRole.CUSTOMER);
                    return userRepository.save(newUser);
                });

    }
    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getPhoneNumber(), user.getRole().name());
    }
}
