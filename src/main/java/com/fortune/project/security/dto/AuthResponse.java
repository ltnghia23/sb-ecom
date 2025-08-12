package com.fortune.project.security.dto;

public record AuthResponse(String accessToken, long expiresInSecond) {
}
