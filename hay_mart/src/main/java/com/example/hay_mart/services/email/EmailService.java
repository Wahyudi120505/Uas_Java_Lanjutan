package com.example.hay_mart.services.email;

public interface EmailService {
    void sendEmail(String to, String subject, String nama);
    void sendOtpEmail(String to, String otp);
    
}
