package com.fortune.project.service.impl;

import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.entity.AppRole;
import com.fortune.project.entity.RoleEntity;
import com.fortune.project.entity.UserEntity;
import com.fortune.project.exception.EmailAlreadyExistsException;
import com.fortune.project.repository.RoleRepository;
import com.fortune.project.repository.UserRepository;
import com.fortune.project.security.dto.LoginRequest;
import com.fortune.project.security.dto.SignUpRequest;
import com.fortune.project.security.dto.UserInfoResponse;
import com.fortune.project.security.jwt.JwtUtils;
import com.fortune.project.security.service.UserDetailsImpl;
import com.fortune.project.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public ResponseEntity<ApiResponse<UserInfoResponse>> authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            UserInfoResponse response = new UserInfoResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    roles,
                    jwtCookie.toString()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new ApiResponse<>("Authenticated successfully", response, LocalDateTime.now()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
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
            RoleEntity userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        RoleEntity adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                    }
                    case "user" -> {
                        RoleEntity adminRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                    }
                    case "seller" -> {
                        RoleEntity adminRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
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


}
