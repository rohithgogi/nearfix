package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    //for admin stats
    Long countByRole(UserRole role);
    Long countByCreatedAtAfter(LocalDateTime date);
    Long countByCreatedAtBetween(LocalDateTime start,LocalDateTime end);
    Page<User> findByRole(UserRole role, Pageable pageable);
}
