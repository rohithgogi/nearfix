package com.nearfix.nearfix.entity;

public enum PaymentStatus {
    PENDING,        // Not paid yet
    PAID,           // Payment completed
    REFUNDED        // Payment refunded after cancellation
}
