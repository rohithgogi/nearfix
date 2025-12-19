package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.PaymentOrderDTO;
import com.nearfix.nearfix.dto.PaymentVerificationDTO;
import com.nearfix.nearfix.entity.Booking;
import com.nearfix.nearfix.entity.BookingStatus;
import com.nearfix.nearfix.repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    private final BookingRepository bookingRepository;

    @PostConstruct
    public void init() throws RazorpayException {
        razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    public PaymentOrderDTO createOrder(Long bookingId) throws RazorpayException{

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if(booking.getStatus()!= BookingStatus.COMPLETED){
            throw new RazorpayException("Only completed bookings can be paid");
        }

        if(booking.getRazorpayPaymentId()!=null){
            throw new RuntimeException("Booking already paid");
        }
        if(booking.getFinalPrice()==null){
            throw new RuntimeException("Booking has no final price set");
        }

        //Create razorpay order
        JSONObject orderRequest=new JSONObject();
        int amountInPaise=booking.getFinalPrice().multiply(BigDecimal.valueOf(100)).intValue();

        orderRequest.put("amount",amountInPaise);
        orderRequest.put("currency","INR");
        orderRequest.put("receipt","booking_"+booking.getId());

        // Add notes for reference
        JSONObject notes = new JSONObject();
        notes.put("booking_id", booking.getId());
        notes.put("customer_phone", booking.getCustomer().getPhoneNumber());
        notes.put("provider_phone", booking.getProvider().getUser().getPhoneNumber());
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);

        // Store order ID in booking
        booking.setRazorpayOrderId(order.get("id"));
        bookingRepository.save(booking);

        return PaymentOrderDTO.builder()
                .orderId(order.get("id"))
                .amount(order.get("amount"))
                .currency(order.get("currency"))
                .keyId(keyId)
                .bookingId(bookingId)
                .customerName(booking.getCustomer().getPhoneNumber())
                .customerPhone(booking.getCustomer().getPhoneNumber())
                .description("Payment for " + booking.getService().getName() + " service")
                .build();

    }

    @Transactional
    public boolean verifyAndUpdatePayment(Long bookingId, PaymentVerificationDTO verificationDTO){
        Booking booking=bookingRepository.findById(bookingId)
                .orElseThrow(()->new RuntimeException("Booking not found"));
        if(!booking.getRazorpayPaymentId().equals(verificationDTO.getOrderId())){
            throw new RuntimeException("Order ID mismatch");
        }

        if(booking.getRazorpayOrderId()!=null){
            throw new RuntimeException("Payment already processed");
        }

        boolean isValid=verifySignature(verificationDTO.getOrderId(),
                verificationDTO.getPaymentId(),
                verificationDTO.getSignature());
        if(!isValid){
            throw new RuntimeException("Invalid payment signature");
        }

        // Update booking with payment details
        booking.setRazorpayPaymentId(verificationDTO.getPaymentId());
        booking.setRazorpaySignature(verificationDTO.getSignature());
        booking.setPaidAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return true;

    }

    private Boolean verifySignature(String orderId, String paymentId, String signature){
        try{
            String payload=orderId+ "|" +paymentId;
            Mac mac=Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec=new SecretKeySpec(keySecret.getBytes(),"HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash= mac.doFinal(payload.getBytes());
            String generatedSignature=HexFormat.of().formatHex(hash);

            return generatedSignature.equals(signature);
        }catch(Exception e){
            throw new RuntimeException("Error verifying signature", e);
        }
    }

    public boolean canBookingBePaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return booking.getStatus() == BookingStatus.COMPLETED
                && booking.getRazorpayPaymentId() == null
                && booking.getFinalPrice() != null;
    }

    public String getPaymentStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getRazorpayPaymentId() != null) {
            return "PAID";
        } else if (booking.getRazorpayOrderId() != null) {
            return "PENDING";
        } else {
            return "NOT_INITIATED";
        }
    }

}
