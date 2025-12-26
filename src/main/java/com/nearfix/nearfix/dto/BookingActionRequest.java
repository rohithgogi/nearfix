package com.nearfix.nearfix.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingActionRequest {
    private String reason; // For rejection or cancellation
}
