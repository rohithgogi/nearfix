package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
    Optional<Provider> findByUserId(Long userId);

    /**
     * Find nearby providers using Haversine formula
     * Filters for verified, profile completed, and available providers
     * Returns providers within specified radius that offer the requested service
     */
    @Query(value =
            "SELECT DISTINCT p.* " +
                    "FROM providers p " +
                    "INNER JOIN provider_services ps ON p.id = ps.provider_id " +
                    "WHERE p.verified = true " +
                    "AND p.profile_completed = true " +
                    "AND p.availability_status = 'AVAILABLE' " +
                    "AND ps.service_id = :serviceId " +
                    "AND ps.available = true " +
                    "AND p.latitude IS NOT NULL " +
                    "AND p.longitude IS NOT NULL " +
                    "AND (6371 * acos(GREATEST(-1, LEAST(1, " +
                    "cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
                    "cos(radians(p.longitude) - radians(:longitude)) + " +
                    "sin(radians(:latitude)) * sin(radians(p.latitude)))))) <= :radiusKm " +
                    "ORDER BY (6371 * acos(GREATEST(-1, LEAST(1, " +
                    "cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
                    "cos(radians(p.longitude) - radians(:longitude)) + " +
                    "sin(radians(:latitude)) * sin(radians(p.latitude)))))",
            nativeQuery = true)
    List<Provider> findNearbyProviders(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("serviceId") Long serviceId
    );

    /**
     * Alternative: Find all verified providers (for admin or broader searches)
     */
    @Query("SELECT p FROM Provider p WHERE p.verified = true AND p.profileCompleted = true")
    List<Provider> findAllVerifiedProviders();
}