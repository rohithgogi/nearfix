package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.ProviderDetailDTO;
import com.nearfix.nearfix.dto.ProviderSearchRequest;
import com.nearfix.nearfix.dto.ProviderSearchResultDTO;
import com.nearfix.nearfix.entity.Provider;
import com.nearfix.nearfix.entity.ProviderService;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.ProviderServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.repository.query.QueryUtils.applySorting;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderSearchService {
    private final ProviderRepository providerRepository;
    private final ProviderServiceRepository providerServiceRepository;

    public List<ProviderSearchResultDTO> searchProviders(ProviderSearchRequest request){
        log.info("Searching providers for service {} at ({}, {}) within {} km",
                request.getServiceId(), request.getLatitude(), request.getLongitude(), request.getRadiusKm());

        List<Provider> nearbyProviders=providerRepository.findNearbyProviders(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusKm(),
                request.getServiceId()
        );
        log.info("Found {} nearby providers", nearbyProviders.size());

        List<ProviderSearchResultDTO> results = nearbyProviders.stream()
                .map(provider -> {
                    Double distance = calculateDistance(
                            request.getLatitude(), request.getLongitude(),
                            provider.getLatitude().doubleValue(), provider.getLongitude().doubleValue()
                    );
                    return buildSearchResult(provider, distance);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Apply filters
        results = applyFilters(results, request);

        // Apply sorting
        results = applySorting(results, request);

        log.info("Returning {} filtered and sorted results", results.size());
        return results;

    }



    public ProviderDetailDTO getProviderDetail(Long providerId) {
        log.info("Fetching provider detail for ID: {}", providerId);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        return buildProviderDetail(provider);
    }

    private ProviderSearchResultDTO buildSearchResult(Provider provider, Double distance) {
        // Get all services offered by this provider
        List<ProviderService> services = providerServiceRepository
                .findByProviderIdAndAvailableTrue(provider.getId());

        if (services.isEmpty()) {
            return null; // Skip providers with no services
        }

        // Get starting price (minimum price among all services)
        BigDecimal startingPrice = services.stream()
                .map(ProviderService::getBasePrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // Build service list
        List<ProviderSearchResultDTO.ServiceOfferedDTO> servicesOffered = services.stream()
                .map(ps -> ProviderSearchResultDTO.ServiceOfferedDTO.builder()
                        .serviceId(ps.getService().getId())
                        .serviceName(ps.getService().getName())
                        .serviceIcon(ps.getService().getIconEmoji())
                        .price(ps.getBasePrice())
                        .build())
                .collect(Collectors.toList());

        return ProviderSearchResultDTO.builder()
                .providerId(provider.getId())
                .businessName(provider.getBusinessName())
                .photoUrl(provider.getPhotoUrl())
                .rating(provider.getRating())
                .totalReviews(provider.getTotalReviews())
                .distanceKm(Math.round(distance * 100.0) / 100.0) // Round to 2 decimals
                .startingPrice(startingPrice)
                .servicesOffered(servicesOffered)
                .address(provider.getAddress())
                .city(provider.getCity())
                .experienceYears(provider.getExperienceYears())
                .verified(provider.getVerified())
                .build();
    }

    private ProviderDetailDTO buildProviderDetail(Provider provider) {
        List<ProviderService> services = providerServiceRepository
                .findByProviderIdAndAvailableTrue(provider.getId());

        List<ProviderSearchResultDTO.ServiceOfferedDTO> servicesOffered = services.stream()
                .map(ps -> ProviderSearchResultDTO.ServiceOfferedDTO.builder()
                        .serviceId(ps.getService().getId())
                        .serviceName(ps.getService().getName())
                        .serviceIcon(ps.getService().getIconEmoji())
                        .price(ps.getBasePrice())
                        .build())
                .collect(Collectors.toList());

        return ProviderDetailDTO.builder()
                .providerId(provider.getId())
                .businessName(provider.getBusinessName())
                .photoUrl(provider.getPhotoUrl())
                .rating(provider.getRating())
                .totalReviews(provider.getTotalReviews())
                .totalBookings(provider.getTotalBookings())
                .address(provider.getAddress())
                .city(provider.getCity())
                .pincode(provider.getPincode())
                .bio(provider.getBio())
                .experienceYears(provider.getExperienceYears())
                .workingHours(provider.getWorkingHours())
                .verificationStatus(provider.getVerificationStatus())
                .services(servicesOffered)
                .recentReviews(new ArrayList<>()) // Placeholder
                .build();
    }

    private List<ProviderSearchResultDTO> applyFilters(
            List<ProviderSearchResultDTO> results,
            ProviderSearchRequest request) {

        return results.stream()
                .filter(result -> {
                    // Price filter
                    if (request.getMinPrice() != null &&
                            result.getStartingPrice().compareTo(request.getMinPrice()) < 0) {
                        return false;
                    }
                    if (request.getMaxPrice() != null &&
                            result.getStartingPrice().compareTo(request.getMaxPrice()) > 0) {
                        return false;
                    }

                    // Rating filter
                    if (request.getMinRating() != null &&
                            result.getRating().doubleValue() < request.getMinRating()) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<ProviderSearchResultDTO> applySorting(
            List<ProviderSearchResultDTO> results,
            ProviderSearchRequest request) {

        Comparator<ProviderSearchResultDTO> comparator;

        switch (request.getSortBy().toLowerCase()) {
            case "rating":
                comparator = Comparator.comparing(ProviderSearchResultDTO::getRating);
                break;
            case "price":
                comparator = Comparator.comparing(ProviderSearchResultDTO::getStartingPrice);
                break;
            case "distance":
            default:
                comparator = Comparator.comparing(ProviderSearchResultDTO::getDistanceKm);
                break;
        }

        if ("desc".equalsIgnoreCase(request.getSortOrder())) {
            comparator = comparator.reversed();
        }

        return results.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    // User Haversine formula
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
