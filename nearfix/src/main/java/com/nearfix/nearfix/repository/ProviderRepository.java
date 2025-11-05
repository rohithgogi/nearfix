package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
    Optional<Provider> findByUserId(Long userId);
}
