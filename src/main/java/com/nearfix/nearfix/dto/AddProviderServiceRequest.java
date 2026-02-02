package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProviderServiceRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price is too high")
    private BigDecimal basePrice;

    // ✅ FIXED: Changed from @Min(0) to @Min(-1) with custom validation
    // This allows null values and 0+ values, but rejects negative numbers
    @Min(value = -1, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience years seems unrealistic")
    private Integer experienceYears;

    @Size(max = 500, message = "Description is too long")
    private String description;

    // ✅ Custom validation method (called in service layer)
    public void validate() {
        if (experienceYears != null && experienceYears < 0) {
            throw new IllegalArgumentException("Experience cannot be negative");
        }
    }
}