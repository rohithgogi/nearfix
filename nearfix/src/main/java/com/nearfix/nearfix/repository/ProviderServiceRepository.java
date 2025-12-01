package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderServiceRepository extends JpaRepository<ProviderService, Long> {

    List<ProviderService> findByProviderId(Long providerId);

    List<ProviderService> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    Optional<ProviderService> findByProviderIdAndServiceId(Long providerId, Long serviceId);

    List<ProviderService> findByProviderIdAndAvailableTrue(Long providerId);

    @Query("SELECT ps FROM ProviderService ps WHERE ps.provider.id = :providerId AND ps.id = :id")
    Optional<ProviderService> findByProviderIdAndId(@Param("providerId") Long providerId,
                                                    @Param("id") Long id);

    boolean existsByProviderIdAndServiceId(Long providerId, Long serviceId);
}