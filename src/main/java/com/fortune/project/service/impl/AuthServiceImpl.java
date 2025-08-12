package com.fortune.project.service.impl;

import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.entity.AppRole;
import com.fortune.project.entity.RoleEntity;
import com.fortune.project.entity.UserEntity;
import com.fortune.project.exception.ApiException;
import com.fortune.project.exception.EmailAlreadyExistsException;
import com.fortune.project.exception.ResourceNotFoundException;
import com.fortune.project.repository.RoleRepository;
import com.fortune.project.repository.UserRepository;
import com.fortune.project.security.dto.AuthResponse;
import com.fortune.project.security.dto.LoginRequest;
import com.fortune.project.security.dto.SignUpRequest;
import com.fortune.project.security.jwt.JwtService;
import com.fortune.project.security.service.UserDetailsImpl;
import com.fortune.project.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshCookieName;
    @Value("${app.jwt.refresh-token-ttl}")
    private long refreshTtl;
    @Value("${app.jwt.refresh-cookie-domain}")
    private String cookieDomain;
    @Value("${app.jwt.refresh-cookie-secure}")
    private boolean cookieSecure;
    @Value("${app.jwt.refresh-cookie-samesite}")
    private String cookieSameSite; // Strict/Lax/None

    @Override
    public ResponseEntity<AuthResponse> authenticateUser(LoginRequest loginRequest, HttpServletResponse res) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())
        );

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        var roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);

        String access = jwtService.generateAccessToken(principal.getUsername(), roles);
        String refresh = jwtService.generateRefreshToken(principal.getUsername(), UUID.randomUUID().toString());

        setRefreshCookie(res, refresh, refreshTtl);
        long expiresIn = Duration.ofSeconds(900).toSeconds();

        return ResponseEntity.ok(new AuthResponse(access, expiresIn));
    }

    @Override
    @Transactional
    public ApiResponse<?> createUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException(signUpRequest.getEmail());
        }
        UserEntity user = new UserEntity(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword())
        );

        Set<String> strRoles = signUpRequest.getRoles();
        Set<RoleEntity> roles = new HashSet<>();

        if (strRoles == null) {
            RoleEntity userRole = roleRepository.findByRoleName(AppRole.USER)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        RoleEntity adminRole = roleRepository.findByRoleName(AppRole.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                    }
                    case "user" -> {
                        RoleEntity adminRole = roleRepository.findByRoleName(AppRole.USER)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                    }
                    case "seller" -> {
                        RoleEntity adminRole = roleRepository.findByRoleName(AppRole.SELLER)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        UserEntity userSaved = userRepository.save(user);
        return new ApiResponse<>("Created user success", userSaved, LocalDateTime.now());
    }

    @Override
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse res) {
        if (refreshToken == null) {
            throw new ApiException("Missing refresh token");
        }

        var jws = jwtService.parse(refreshToken);
        if (!"refresh".equals(jws.getPayload().get("token_type", String.class))) {
            throw new ApiException("Wrong token Type");
        }

        String username = jws.getPayload().getSubject();
        var user = userRepository.findByName(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        var roles = user.getRoles().toArray(new String[0]);
        String access = jwtService.generateAccessToken(username, roles);
        // tuỳ chọn: rotate refresh token mỗi lần gọi
        String newRefresh = jwtService.generateRefreshToken(username, UUID.randomUUID().toString());
        setRefreshCookie(res, newRefresh, refreshTtl);
        return new AuthResponse(access, 900);
    }

    @Override
    public ApiResponse<?> logout(HttpServletResponse res) {
        // Xoá cookie refresh
        Cookie cookie = new Cookie(refreshCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(0);
        cookie.setDomain(cookieDomain);
        res.addCookie(cookie);
        return new ApiResponse<>(null, "Logged out", LocalDateTime.now());
    }

    private void setRefreshCookie(HttpServletResponse res, String value, long ttlSeconds) {
        Cookie cookie = new Cookie(refreshCookieName, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge((int) ttlSeconds);
        if (cookieDomain != null && !cookieDomain.isBlank()) cookie.setDomain(cookieDomain);
        // Spring không có setter SameSite trên Cookie; thêm header thủ công:
        res.addHeader("Set-Cookie", String.format("%s=%s; Max-Age=%d; Path=/api/auth/refresh; Domain=%s; HttpOnly; Secure; SameSite=%s",
                refreshCookieName, value, ttlSeconds, cookieDomain, cookieSameSite));
    }


}
