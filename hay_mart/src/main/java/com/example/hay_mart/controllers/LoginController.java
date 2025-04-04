package com.example.hay_mart.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.login.LoginRequest;
import com.example.hay_mart.dto.login.RegisRequest;
import com.example.hay_mart.services.login.LoginService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@Slf4j
public class LoginController {
    @Autowired 
    LoginService loginService;
    
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok().body(GenericResponse.success(loginService.login(request), "Successfully login"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisRequest request) {
        try {
            return ResponseEntity.ok().body(GenericResponse.success(loginService.register(request), "Successfully registered"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }
}