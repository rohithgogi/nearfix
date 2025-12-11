package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.*;
import com.nearfix.nearfix.security.JwtTokenProvider;
import com.nearfix.nearfix.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5175")
public class BookingController {

    private final BookingService bookingService;
    private final JwtTokenProvider jwtTokenProvider;

    // Customer endpoints
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(
            HttpServletRequest request,
            @Valid @RequestBody CreateBookingRequest bookingRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            BookingDTO booking = bookingService.createBooking(phoneNumber, bookingRequest);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<List<BookingDTO>> getCustomerBookings(
            HttpServletRequest request,
            @RequestParam(required = false) String status) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            List<BookingDTO> bookings = bookingService.getCustomerBookings(phoneNumber, status);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching customer bookings: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // Provider endpoints
    @GetMapping("/provider")
    public ResponseEntity<List<BookingDTO>> getProviderBookings(
            HttpServletRequest request,
            @RequestParam(required = false) String status) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            List<BookingDTO> bookings = bookingService.getProviderBookings(phoneNumber, status);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching provider bookings: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<BookingDTO> acceptBooking(
            HttpServletRequest request,
            @PathVariable Long id) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            BookingDTO booking = bookingService.acceptBooking(phoneNumber, id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error accepting booking: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingDTO> rejectBooking(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody BookingActionRequest actionRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            BookingDTO booking = bookingService.rejectBooking(phoneNumber, id, actionRequest);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error rejecting booking: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingDTO> completeBooking(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody CompleteBookingRequest completeRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            BookingDTO booking = bookingService.completeBooking(phoneNumber, id, completeRequest);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error completing booking: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody BookingActionRequest actionRequest) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            BookingDTO booking = bookingService.cancelBooking(phoneNumber, id, actionRequest);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(
            HttpServletRequest request,
            @PathVariable Long id) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            BookingDTO booking = bookingService.getBookingById(phoneNumber, id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error fetching booking: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getPhoneNumberFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtTokenProvider.getPhoneNumberFromToken(token);
        }
        throw new RuntimeException("No authentication token found");
    }
}