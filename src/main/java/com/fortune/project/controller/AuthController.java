package com.fortune.project.controller;

import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.security.dto.LoginRequest;
import com.fortune.project.security.dto.SignUpRequest;
import com.fortune.project.security.dto.UserInfoResponse;
import com.fortune.project.security.jwt.JwtUtils;
import com.fortune.project.security.service.UserDetailsImpl;
import com.fortune.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.createUser(signUpRequest));
    }

    @GetMapping("/username")
    public String currentUsername(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        } else {
            return "Null";
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles);

        return ResponseEntity.ok().body(new ApiResponse<>("Authenticated successfully", response, LocalDateTime.now()));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser(){
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ApiResponse<>("Signed out", null, LocalDateTime.now()));
    }

}
