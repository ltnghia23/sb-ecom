package com.fortune.project.service;

import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.security.dto.LoginRequest;
import com.fortune.project.security.dto.SignUpRequest;
import com.fortune.project.security.dto.UserInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

public interface AuthService {
    ResponseEntity<ApiResponse<UserInfoResponse>> authenticateUser(LoginRequest loginRequest) throws BadCredentialsException;

    ApiResponse<?> createUser(SignUpRequest signUpRequest);
}
