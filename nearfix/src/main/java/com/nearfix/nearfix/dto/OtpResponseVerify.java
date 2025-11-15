package com.nearfix.nearfix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponseVerify {
    private boolean newUser;
    private String phoneNumber;
    private String jwtToken;
    private String role;

}
