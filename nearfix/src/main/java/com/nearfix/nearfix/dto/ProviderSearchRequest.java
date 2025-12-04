package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSearchRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @DecimalMin(value = "1.0", message = "Radius must be at least 1 km")
    @DecimalMax(value = "50.0", message = "Radius cannot exceed 50 km")
    private Double radiusKm = 10.0; // Default 10 km

    // Filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double minRating;

    // Sorting
    private String sortBy = "distance"; // distance, rating, price
    private String sortOrder = "asc"; // asc, desc
}