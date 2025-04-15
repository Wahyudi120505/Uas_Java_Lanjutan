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
import com.example.hay_mart.repositorys.UserRepository;
import com.example.hay_mart.services.email.EmailService;
import com.example.hay_mart.services.login.ForgotPWService;
import com.example.hay_mart.services.login.LoginService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    LoginService loginService;

    @Autowired
    EmailService emailService;

    @Autowired
    ForgotPWService forgotPWService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok().body(GenericResponse.success(loginService.login(request), "Successfully login"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat login", e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan saat login"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisRequest request) {
        try {
            return ResponseEntity.ok().body(GenericResponse.success(
                loginService.register(request),
                "Registrasi berhasil. Silakan periksa email Anda untuk verifikasi."
            ));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat registrasi", e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan saat registrasi"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verifyUser(@RequestParam String email, @RequestParam String code) {
        try {
            emailService.verifyEmail(email, code);
            return ResponseEntity.ok(GenericResponse.success(null, "Email Anda berhasil diverifikasi. Sekarang Anda bisa login."));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat verifikasi email", e);
            return ResponseEntity.badRequest().body(GenericResponse.error("Kode verifikasi salah atau sudah kadaluarsa."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPWRequest request) {
        try {
            forgotPWService.sendtoEmail(request);
            return ResponseEntity.ok(GenericResponse.success(null, "Kode OTP akan segera dikirim melalui email Anda"));
        } catch (Exception e) {
            log.error("Error saat mengirim OTP", e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Gagal mengirim kode OTP ke email"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ResetPWRequest request) {
        try {
            forgotPWService.resetPassword(request);
            return ResponseEntity.ok(GenericResponse.success(null, "Password berhasil diubah"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat reset password", e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan saat mereset password"));
        }
    }
}
