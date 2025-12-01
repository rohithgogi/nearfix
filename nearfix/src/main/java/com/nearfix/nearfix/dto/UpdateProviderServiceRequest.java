package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProviderServiceRequest {

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price is too high")
    private BigDecimal basePrice;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience years seems unrealistic")
    private Integer experienceYears;

    @Size(max = 500, message = "Description is too long")
    private String description;

    private Boolean available;
}