package com.nearfix.nearfix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderSearchResultDTO {
    private Long providerId;
    private String businessName;
    private String photoUrl;
    private BigDecimal rating;
    private Integer totalReviews;
    private Double distanceKm;
    private BigDecimal startingPrice;
    private List<ServiceOfferedDTO> servicesOffered;
    private String address;
    private String city;
    private Integer experienceYears;
    private Boolean verified;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceOfferedDTO {
        private Long serviceId;
        private String serviceName;
        private String serviceIcon;
        private BigDecimal price;
    }
}
