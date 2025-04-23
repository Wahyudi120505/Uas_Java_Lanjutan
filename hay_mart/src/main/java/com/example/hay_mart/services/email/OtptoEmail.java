package com.example.hay_mart.services.email;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
public class OtptoEmail {
    private final Map<String, OtpEntry> otpMap = new ConcurrentHashMap<>();

    public void saveOtp(String email, String otp, LocalDateTime expire) {
        otpMap.put(email, new OtpEntry(otp, expire));
    }

    public boolean isOtpValid(String email, String otp) {
        OtpEntry entry = otpMap.get(email);

        if (entry == null) {
            System.out.println("OTP tidak ditemukan untuk email: " + email);
            return false;
        }

        if (entry.getExpire().isBefore(LocalDateTime.now())) {
            System.out.println("OTP kadaluarsa untuk email: " + email + ", expired at: " + entry.getExpire());
            otpMap.remove(email);
            return false;
        }

        System.out.println("Kode: " + otp + ", expire: " + entry.getExpire() + ", email: " + email);
        return entry.getOtp().equals(otp);
    }

    public void removeOtp(String email) {
        otpMap.remove(email);
    }

    @Getter
    @AllArgsConstructor
    static class OtpEntry {
        private String otp;
        private LocalDateTime expire;
    }
}
