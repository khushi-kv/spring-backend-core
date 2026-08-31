package com.example.basics.dto;
import java.util.Set;

/**
 * Authentication Response DTO returned after successful login or registration.
 * Contains the JWT access token, token type, username, and assigned roles.
 */
public class AuthResponseDto {
    private String message;
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private Set<String> roles;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String message, String accessToken, String username, Set<String> roles) {
        this.message = message;
        this.accessToken = accessToken;
        this.username = username;
        this.roles = roles;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

