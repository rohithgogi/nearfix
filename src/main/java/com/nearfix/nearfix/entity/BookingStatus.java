package com.nearfix.nearfix.entity;

public enum BookingStatus {
    PENDING,        // Just created, waiting for provider response
    ACCEPTED,       // Provider accepted, scheduled
    REJECTED,       // Provider rejected
    IN_PROGRESS,    // Provider started work
    COMPLETED,      // Work completed
    CANCELLED       // Cancelled by customer or provider
}
