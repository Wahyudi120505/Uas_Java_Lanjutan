package com.example.hay_mart.services.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.hay_mart.models.User;
import com.example.hay_mart.repositorys.UserRepository;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    UserRepository userRepository;

    @Value("${app.verification.url}")
    private String verificationUrl;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    PasswordEncoder passwordEncoder;

    public void sendVerificationEmail(String to, String nama, String verificationCode) {
        try {
            String emailBody = loadVerificationTemplate(to, verificationCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Verifikasi Email Anda - HayMart");
            helper.setText(emailBody, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Gagal mengirim email verifikasi", e);
        }
    }

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            String emailBody = loadOtpTemplate(otp);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Kode OTP Reset Password HayMart");
            helper.setText(emailBody, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Gagal mengirim email OTP", e);
        }
    }

    private String loadVerificationTemplate(String email, String code) throws IOException {
        ClassPathResource resource = new ClassPathResource("template/verification-email.html");
        String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        String verificationLink = String.format("%s?email=%s&code=%s", verificationUrl, email, code);
        return content.replace("{{nama}}", email)
                .replace("{{code}}", code)
                .replace("{{verificationUrl}}", verificationLink);
    }

    private String loadOtpTemplate(String otp) throws IOException {
        ClassPathResource resource = new ClassPathResource("template/forgot-pw.html");
        String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        return content.replace("{{otp}}", otp);
    }

    @Override
    // @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Kode verifikasi tidak valid.");
        } else if ("pending".equalsIgnoreCase(user.getStatus())
                && user.getStarDate().plusDays(1).isBefore(LocalDate.now())) {
            // Hapus user pending yang sudah lewat 1 hari
            deletePendingUsers();
            throw new RuntimeException("Pendaftaran telah kadaluwarsa. Silahkan daftar ulang.");
        } else if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            // Kode verifikasi kadaluarsa tapi belum 1 hari atau bukan status pending
            String verificationCode = UUID.randomUUID().toString().substring(0, 8);
            sendVerificationEmail(user.getEmail(), user.getNama(), verificationCode);
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);
            System.out.println(user);
            throw new RuntimeException("Kode verifikasi telah kedaluwarsa. Silahkan Cek email anda kembali!!!");
        }
        user.setIsVerified(true);
        user.setStatus("active");
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);

        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deletePendingUsers() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<User> pendingUsers = userRepository.findByStatusIgnoreCaseAndStarDateBefore("pending", yesterday);
        System.out.println("Jumlah user pending yang kadaluarsa dan akan dihapus: " + pendingUsers.size());
        userRepository.deleteAll(pendingUsers);
    }

}