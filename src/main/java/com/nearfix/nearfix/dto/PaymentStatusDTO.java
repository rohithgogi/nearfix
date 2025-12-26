package com.nearfix.nearfix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusDTO {
    private Long bookingId;
    private String status; // PAID, PENDING, NOT_INITIATED
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime paidAt;
    private BigDecimal amount;
    private boolean canPay;
}