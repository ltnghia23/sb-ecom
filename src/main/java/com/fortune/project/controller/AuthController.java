package com.fortune.project.controller;

import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.security.dto.AuthResponse;
import com.fortune.project.security.dto.LoginRequest;
import com.fortune.project.security.dto.SignUpRequest;
import com.fortune.project.security.dto.UserInfoResponse;
import com.fortune.project.security.service.UserDetailsImpl;
import com.fortune.project.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse res) {
        return authService.authenticateUser(loginRequest, res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "${app.jwt.refresh-cookie-name}", required = false) String refreshToken,
            HttpServletResponse res) {
        AuthResponse response = authService.refreshToken(refreshToken, res);
        return new ResponseEntity<>(response, HttpStatus.OK);

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
                userDetails.getUsername(),
                roles);

        return ResponseEntity.ok().body(new ApiResponse<>("Authenticated successfully", response, LocalDateTime.now()));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser(HttpServletResponse res) {
        ApiResponse<?> response = authService.logout(res);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
