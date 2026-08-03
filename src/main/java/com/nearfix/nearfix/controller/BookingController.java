package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.*;
import com.nearfix.nearfix.security.JwtTokenProvider;
import com.nearfix.nearfix.service.BookingService;
import com.nearfix.nearfix.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStorageService fileStorageService;

    // Upload a problem photo before the booking exists yet.
    // Frontend calls this per photo, collects the returned URLs, then sends them
    // in CreateBookingRequest.photoUrls when the booking is actually submitted.
    @PostMapping("/attachments")
    public ResponseEntity<Map<String, String>> uploadAttachment(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        try {
            // Just confirms the caller is an authenticated customer (route is already
            // restricted to ROLE_CUSTOMER by /api/bookings/** in SecurityConfig)
            getPhoneNumberFromToken(request);
            String url = fileStorageService.storeFile(file, "booking-attachments");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            log.error("Error uploading booking attachment: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

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