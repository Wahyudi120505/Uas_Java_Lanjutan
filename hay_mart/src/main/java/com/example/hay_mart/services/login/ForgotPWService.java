package com.example.hay_mart.services.login;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.hay_mart.dto.login.ForgotPWRequest;
import com.example.hay_mart.dto.login.ResetPWRequest;
import com.example.hay_mart.models.User;
import com.example.hay_mart.repositorys.UserRepository;
import com.example.hay_mart.services.email.EmailService;
import com.example.hay_mart.services.email.OtptoEmail;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForgotPWService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtptoEmail otptoEmail;
    private final PasswordEncoder passwordEncoder;

    public void sendtoEmail(ForgotPWRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan"));
                
        if (!user.getIsVerified() || user.getStatus().equalsIgnoreCase("pending")) {
            throw new RuntimeException("Akun belum diverifikasi atau masih pending");
        }

        String otp = generateOtp();
        otptoEmail.saveOtp(user.getEmail(), otp, LocalDateTime.now().plusMinutes(5));
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public void resetPassword(ResetPWRequest request) {
        boolean valid = otptoEmail.isOtpValid(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new RuntimeException("OTP salah atau sudah kadaluarsa");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidal ditemukan"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otptoEmail.removeOtp(request.getEmail());
    }

    public String generateOtp() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
