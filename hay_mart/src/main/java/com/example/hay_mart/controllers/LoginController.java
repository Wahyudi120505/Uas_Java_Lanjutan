package com.example.hay_mart.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hay_mart.dto.login.LoginRequest;
import com.example.hay_mart.dto.login.LoginResponse;
import com.example.hay_mart.dto.login.RegisRequest;
import com.example.hay_mart.services.login.LoginService;


@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisRequest request){
        String message = loginService.register(request);
        return ResponseEntity.ok(message);
    }

}
