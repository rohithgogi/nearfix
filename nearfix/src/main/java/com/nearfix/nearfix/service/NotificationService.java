package com.nearfix.nearfix.service;

import com.nearfix.nearfix.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SnsService snsService;
    private static final DateTimeFormatter DATE_FORMAT=DateTimeFormatter.ofPattern("dd MMM yyy,hh:mm a");

    public void notifyBookingCreated(Booking booking){
        String message = String.format(
                "New booking request #%d from customer. Service: %s on %s. Check NearFix app to accept/reject.",
                booking.getId(),
                booking.getService().getName(),
                booking.getScheduledDateTime().format(DATE_FORMAT)
        );
        log.info("📲 NOTIFICATION (Provider): {}", message);

        try {
            // Uncomment when SMS is enabled
            // snsService.sendSms(booking.getProvider().getUser().getPhoneNumber(), message);
        } catch (Exception e) {
            log.error("Failed to send booking created notification: {}", e.getMessage());
        }
    }

    public void notifyBookingAccepted(Booking booking){
        String message = String.format(
                "Your booking #%d has been ACCEPTED by %s for %s. They will contact you soon!",
                booking.getId(),
                booking.getProvider().getBusinessName(),
                booking.getScheduledDateTime().format(DATE_FORMAT)
        );

        log.info("📲 NOTIFICATION (Customer): {}", message);

        try {
            // snsService.sendSms(booking.getCustomer().getPhoneNumber(), message);
        } catch (Exception e) {
            log.error("Failed to send booking accepted notification: {}", e.getMessage());
        }
    }

    public void notifyBookingRejected(Booking booking) {
        String message = String.format(
                "Your booking #%d has been REJECTED by %s. Reason: %s. Please book another provider.",
                booking.getId(),
                booking.getProvider().getBusinessName(),
                booking.getCancellationReason() != null ? booking.getCancellationReason() : "Not specified"
        );

        log.info("📲 NOTIFICATION (Customer): {}", message);

        try {
            // snsService.sendSms(booking.getCustomer().getPhoneNumber(), message);
        } catch (Exception e) {
            log.error("Failed to send booking rejected notification: {}", e.getMessage());
        }
    }

    public void notifyBookingCompleted(Booking booking) {
        String message = String.format(
                "Your booking #%d has been COMPLETED. Final amount: ₹%.2f. Please rate the service in the app.",
                booking.getId(),
                booking.getFinalPrice()
        );

        log.info("📲 NOTIFICATION (Customer): {}", message);

        try {
            // snsService.sendSms(booking.getCustomer().getPhoneNumber(), message);
        } catch (Exception e) {
            log.error("Failed to send booking completed notification: {}", e.getMessage());
        }
    }

    public void notifyBookingCancelled(Booking booking, boolean isCustomerCancellation) {
        if (isCustomerCancellation) {
            // Notify provider
            String message = String.format(
                    "Booking #%d for %s has been CANCELLED by the customer.",
                    booking.getId(),
                    booking.getScheduledDateTime().format(DATE_FORMAT)
            );
            log.info("📲 NOTIFICATION (Provider): {}", message);
            // snsService.sendSms(booking.getProvider().getUser().getPhoneNumber(), message);
        } else {
            // Notify customer
            String message = String.format(
                    "Your booking #%d has been CANCELLED by %s. Reason: %s",
                    booking.getId(),
                    booking.getProvider().getBusinessName(),
                    booking.getCancellationReason() != null ? booking.getCancellationReason() : "Not specified"
            );
            log.info("📲 NOTIFICATION (Customer): {}", message);
            // snsService.sendSms(booking.getCustomer().getPhoneNumber(), message);
        }
    }
}
