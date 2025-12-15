package com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs;

import com.ahsmart.campusmarket.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {

    // Indicates whether authentication succeeded
    private boolean success;

    // Message used for UI feedback
    private String message;

    // Role of the authenticated user
    private Role role;

    // Logged-in user's ID
    private Long userId;

    // Logged-in user's first name (for UI greeting)
    private String name;

    // Factory method for failed login
    public static LoginResult failed(String message) {
        return new LoginResult(false, message, null, null, null);
    }

    // Factory method for successful login
    public static LoginResult success(Role role, Long userId, String name) {
        return new LoginResult(true, "Login successful", role, userId, name);
    }
}
