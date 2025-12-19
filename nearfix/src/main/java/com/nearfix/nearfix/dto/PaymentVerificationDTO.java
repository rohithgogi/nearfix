package com.nearfix.nearfix.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationDTO {
    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Payment ID is required")
    private String paymentId;

    @NotNull(message = "Signature is required")
    private String signature;
}