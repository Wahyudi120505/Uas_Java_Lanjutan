package com.example.hay_mart.services.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService{
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender=mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String nama){
        try {
            String emailBody = loadEmailTemplate(nama);
    
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(emailBody, true);
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengirim email",e);
        }

    }

    private String loadEmailTemplate(String nama) throws IOException{
        ClassPathResource resource = new ClassPathResource("template/email.html");
        String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        return content.replace("{{nama}}", nama);
    }

    @Override
    public void sendOtpEmail(String to, String otp){
        try {
            String emailBody = loadOtpTemplate(otp);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Kode OTP Reset Password Haymart");
            helper.setText(emailBody, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Gagal mengirim email OTP",e);
        }
    }

    private String loadOtpTemplate(String otp) throws IOException {
        ClassPathResource resource = new ClassPathResource("template/forgot-pw.html");
        String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        return content.replace("{{otp}}", otp);
    }
}
