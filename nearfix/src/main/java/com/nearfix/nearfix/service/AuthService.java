package com.nearfix.nearfix.service;

import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.UserRole;
import com.nearfix.nearfix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    public User loginOrRegister(String phoneNumber){
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(()->{
                    User newUser=new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setRole(UserRole.CUSTOMER);
                    return userRepository.save(newUser);
                });

    }
}
