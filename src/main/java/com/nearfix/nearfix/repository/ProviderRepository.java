package com.nearfix.nearfix.repository;

import com.nearfix.nearfix.entity.Provider;
import com.nearfix.nearfix.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
    Optional<Provider> findByUserId(Long userId);

    /**
     * Find providers using Haversine formula for distance, ordered by distance ascending.
     * Filters for verified, profile completed, and available providers.
     * No radius cutoff — returns every eligible provider so the caller can filter/sort
     * (by distance, price, rating, etc.) rather than silently excluding far-away matches.
     */
    @Query(value = """
    SELECT DISTINCT p.*,
    (
        6371 * acos(
            GREATEST(-1, LEAST(1,
                cos(radians(:latitude)) 
                * cos(radians(p.latitude)) 
                * cos(radians(p.longitude) - radians(:longitude)) 
                + sin(radians(:latitude)) * sin(radians(p.latitude))
            ))
        )
    ) AS distance
    FROM providers p
    INNER JOIN provider_services ps ON p.id = ps.provider_id
    WHERE p.verified = true
      AND p.profile_completed = true
      AND p.availability_status = 'AVAILABLE'
      AND ps.service_id = :serviceId
      AND ps.available = true
      AND p.latitude IS NOT NULL
      AND p.longitude IS NOT NULL
    ORDER BY distance ASC
    """, nativeQuery = true)
    List<Provider> findProvidersByService(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("serviceId") Long serviceId
    );

    /**
     * Alternative: Find all verified providers (for admin or broader searches)
     */
    @Query("SELECT p FROM Provider p WHERE p.verified = true AND p.profileCompleted = true")
    List<Provider> findAllVerifiedProviders();

    //for Admin stats
    Long countByVerificationStatus(VerificationStatus status);
    Long countByVerified(Boolean verified);
    Page<Provider> findByVerificationStatusIn(List<VerificationStatus> statuses, Pageable pageable);
    List<Provider> findTop5ByOrderByCreatedAtDesc();
}