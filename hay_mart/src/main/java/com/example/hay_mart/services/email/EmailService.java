package com.example.hay_mart.services.email;

public interface EmailService {
    void sendVerificationEmail(String to, String nama, String verificationCode);
    void sendOtpEmail(String to, String otp);
    void verifyEmail(String email, String code);
    void deletePendingUsers();
}
