package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.PaymentOrderDTO;
import com.nearfix.nearfix.dto.PaymentStatusDTO;
import com.nearfix.nearfix.dto.PaymentVerificationDTO;
import com.nearfix.nearfix.service.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createPaymentOrder(@RequestParam Long bookingId) {
        try {
            log.info("Creating payment order for booking: {}", bookingId);
            PaymentOrderDTO orderDTO = paymentService.createOrder(bookingId);

            log.info("Payment order created: {}", orderDTO.getOrderId());
            return ResponseEntity.ok(orderDTO);
        } catch (RazorpayException e) {
            log.error("Razorpay error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create payment order: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating payment order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> verifyPayment(
            @RequestParam Long bookingId,
            @Valid @RequestBody PaymentVerificationDTO verificationDTO
    ) {
        try {
            log.info("Verifying payment for booking: {}", bookingId);

            boolean verified = paymentService.verifyAndUpdatePayment(bookingId, verificationDTO);

            if (verified) {
                log.info("Payment verified successfully for booking: {}", bookingId);
                return ResponseEntity.ok().body(
                        PaymentStatusDTO.builder()
                                .bookingId(bookingId)
                                .status("PAID")
                                .razorpayPaymentId(verificationDTO.getPaymentId())
                                .build()
                );
            } else {
                log.warn("Payment verification failed for booking: {}", bookingId);
                return ResponseEntity.badRequest().body("Payment verification failed");
            }

        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<?> getPaymentStatus(@RequestParam Long bookingId) {
        try {
            String status = paymentService.getPaymentStatus(bookingId);
            return ResponseEntity.ok().body(
                    PaymentStatusDTO.builder()
                            .bookingId(bookingId)
                            .status(status)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

