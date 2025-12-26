package com.nearfix.nearfix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceDTO {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private String serviceIcon;
    private BigDecimal basePrice;
    private Integer experienceYears;
    private String description;
    private Boolean available;
    private LocalDateTime createdAt;
}