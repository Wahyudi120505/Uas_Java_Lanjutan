package com.example.hay_mart.services.login;

import com.example.hay_mart.dto.login.LoginRequest;
import com.example.hay_mart.dto.login.LoginResponse;
import com.example.hay_mart.dto.login.RegisRequest;

public interface LoginService {
    LoginResponse login(LoginRequest request);

    String register(RegisRequest request);
}
