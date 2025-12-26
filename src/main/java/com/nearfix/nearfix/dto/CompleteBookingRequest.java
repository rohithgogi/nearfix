package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteBookingRequest {

    @NotNull(message = "Final price is required")
    @DecimalMin(value = "0.01", message = "Final price must be positive")
    @DecimalMax(value = "99999.99", message = "Final price is too high")
    private BigDecimal finalPrice;

    @Size(max = 1000, message = "Notes too long (max 1000 characters)")
    private String notes;

    /**
     * Custom validation logic
     */
    public void validate() {
        if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final price must be greater than 0");
        }

        if (finalPrice.compareTo(new BigDecimal("99999.99")) > 0) {
            throw new IllegalArgumentException("Final price is too high (max ₹99,999.99)");
        }
    }
}