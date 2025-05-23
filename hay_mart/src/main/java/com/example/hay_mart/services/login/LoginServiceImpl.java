package com.example.hay_mart.services.login;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.hay_mart.constant.RoleConstant;
import com.example.hay_mart.dto.login.LoginRequest;
import com.example.hay_mart.dto.login.LoginResponse;
import com.example.hay_mart.dto.login.RegisRequest;
import com.example.hay_mart.filter.JwtUtil;
import com.example.hay_mart.models.User;
import com.example.hay_mart.repositorys.RoleRepository;
import com.example.hay_mart.repositorys.UserRepository;
import com.example.hay_mart.services.CustomUserDetails;
import com.example.hay_mart.services.email.EmailService;

@Service
public class LoginServiceImpl implements LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public LoginServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService,
            RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Akun tidak ada/tidak terdaftar"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email Atau Password Salah");
        }

        if (RoleConstant.ROLE_KASIR.equalsIgnoreCase(user.getRole().getRoleName())) {
            if (!"active".equalsIgnoreCase(user.getStatus()) || !Boolean.TRUE.equals(user.getIsVerified())) {
                throw new RuntimeException("Kasir tidak dapat login karena statusnya belum aktif atau belum diverifikasi");
            }
        }
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token, user.getRole().getRoleName(), user.getNama());
    }

    @Override
    @Transactional
    public String register(RegisRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar");
        }

        try {
            Resource resource = new ClassPathResource("static/images/default.png");
            byte[] imageBytes;
            try {
                imageBytes = Files.readAllBytes(resource.getFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException("Gagal memuat gambar default", e);
            }

            String verificationCode = UUID.randomUUID().toString().substring(0, 8);

            User newUser = User.builder()
                    .nama(request.getNama())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .status("pending")
                    .role(roleRepository.findRoleByRoleName(RoleConstant.ROLE_KASIR))
                    .image(new SerialBlob(imageBytes))
                    .starDate(LocalDate.now())
                    .verificationCode(verificationCode)
                    .verificationCodeExpiry(LocalDateTime.now().plusMinutes(5))
                    .isVerified(false)
                    .build();

            userRepository.save(newUser);

            emailService.sendVerificationEmail(request.getEmail(), request.getNama(), verificationCode);

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memproses gambar default: " + e.getMessage());
        }
    }
}