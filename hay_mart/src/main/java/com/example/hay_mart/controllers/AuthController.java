package com.example.hay_mart.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.login.ForgotPWRequest;
import com.example.hay_mart.dto.login.LoginRequest;
import com.example.hay_mart.dto.login.RegisRequest;
import com.example.hay_mart.dto.login.ResetPWRequest;
import com.example.hay_mart.services.email.EmailService;
import com.example.hay_mart.services.login.ForgotPWService;
import com.example.hay_mart.services.login.LoginService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPWService forgotPWService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            var data = loginService.login(request);
            return ResponseEntity.ok().body(GenericResponse.success(data, "Login berhasil"));
        } catch (ResponseStatusException e) {
            log.info("Login gagal: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat login: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan saat login"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisRequest request) {
        try {
            var data = loginService.register(request);
            return ResponseEntity.ok().body(GenericResponse.success(data, "Silakan periksa email Anda untuk verifikasi."));
        } catch (ResponseStatusException e) {
            log.info("Registrasi gagal: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat registrasi: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan saat registrasi"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verifyUser(@RequestParam String email, @RequestParam String code) {
        try {
            emailService.verifyEmail(email, code);
            return ResponseEntity.ok(GenericResponse.success(null, "Email berhasil diverifikasi. Silakan login."));
        } catch (ResponseStatusException e) {
            log.info("Verifikasi gagal: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat verifikasi email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(GenericResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPWRequest request) {
        try {
            forgotPWService.sendtoEmail(request);
            return ResponseEntity.ok(GenericResponse.success(null, "Kode OTP berhasil dikirim ke email Anda."));
        } catch (Exception e) {
            log.error("Error saat mengirim OTP: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Gagal mengirim kode OTP ke email."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ResetPWRequest request) {
        try {
            forgotPWService.resetPassword(request);
            return ResponseEntity.ok(GenericResponse.success(null, "Password berhasil diubah."));
        } catch (ResponseStatusException e) {
            log.info("Reset password gagal: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat reset password: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan saat mereset password."));
        }
    }
}
