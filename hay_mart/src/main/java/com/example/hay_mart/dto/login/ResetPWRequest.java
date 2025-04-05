package com.example.hay_mart.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPWRequest {
    private String email;
    private String otp;
    private String newPassword;
}
