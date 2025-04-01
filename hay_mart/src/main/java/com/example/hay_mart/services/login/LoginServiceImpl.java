package com.example.hay_mart.services.login;

import java.sql.Blob;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.hay_mart.constant.RoleConstant;
import com.example.hay_mart.dto.login.LoginRequest;
import com.example.hay_mart.dto.login.LoginResponse;
import com.example.hay_mart.dto.login.RegisRequest;
import com.example.hay_mart.filter.JwtUtil;
import com.example.hay_mart.models.Role;
import com.example.hay_mart.models.User;
import com.example.hay_mart.repositorys.UserRepository;
import com.example.hay_mart.services.email.EmailService;

@Service
public class LoginServiceImpl implements LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public LoginServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Akun tidak ada/tidak terdaftar"));


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("invalid credential");
        }

        if (RoleConstant.ROLE_KASIR.equalsIgnoreCase(user.getRole().getRoleName())
                && !"active".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Kasir tidak dapat login karena statusnnya sudah tidak active");

        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        // Generate JWT token dengan user yang sudah terautentikasi
        String token = jwtUtil.generateToken(userDetails);
        return new LoginResponse(token, user.getRole().getRoleName());
    }

    public String register(RegisRequest  request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar");
        }

        Role kasiRole = new Role();
        kasiRole.setRoleId(2);
        kasiRole.setRoleName(RoleConstant.ROLE_KASIR);

        byte[] defaultImageBytes = "hhh".getBytes();
        Blob defaultImageBlob = BlobProxy.generateProxy(defaultImageBytes);

        User newUser = User.builder()
                .nama(request.getNama())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status("active")
                .role(kasiRole)
                .image(defaultImageBlob)
                .build();

        userRepository.save(newUser);
        emailService.sendEmail(request.getEmail(), "registrasi anda berhasil, silahkan cek email", request.getNama());
        return "Registrasi berhasil";
    }
}
