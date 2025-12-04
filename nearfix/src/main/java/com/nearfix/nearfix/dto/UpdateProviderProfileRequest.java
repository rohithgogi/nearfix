package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProviderProfileRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name is too long")
    private String businessName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private BigDecimal longitude;

    @Size(max = 1000, message = "Bio is too long")
    private String bio;

    private String workingHours;  // JSON string

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience years seems unrealistic")
    private Integer experienceYears;
}