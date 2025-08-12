package com.fortune.project.service;

import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.security.dto.AuthResponse;
import com.fortune.project.security.dto.LoginRequest;
import com.fortune.project.security.dto.SignUpRequest;
import com.fortune.project.security.dto.UserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

public interface AuthService {
    ResponseEntity<AuthResponse> authenticateUser(LoginRequest loginRequest, HttpServletResponse res) throws BadCredentialsException;

    ApiResponse<?> createUser(SignUpRequest signUpRequest);

    AuthResponse refreshToken(String refreshToken, HttpServletResponse res);

    ApiResponse<?> logout(HttpServletResponse res);
}
