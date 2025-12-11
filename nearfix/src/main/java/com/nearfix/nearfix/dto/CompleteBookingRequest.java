package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteBookingRequest {

    @NotNull(message = "Final price is required")
    @DecimalMin(value = "0.01", message = "Final price must be positive")
    private BigDecimal finalPrice;

    private String notes;
}