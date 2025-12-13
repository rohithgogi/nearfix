package com.nearfix.nearfix.dto;

import com.nearfix.nearfix.entity.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvailabilityRequest {

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus availabilityStatus;
}