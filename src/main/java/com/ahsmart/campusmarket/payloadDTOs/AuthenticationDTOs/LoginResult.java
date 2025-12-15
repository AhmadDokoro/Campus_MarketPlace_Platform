package com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs;


import com.ahsmart.campusmarket.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LoginResult {

    private boolean success;
    private String message;
    private Role role;
    private Long userId;
    private String name;


    public static LoginResult failed(String message) {
        return new LoginResult(false, message, null, null,null);
    }

    public static LoginResult success(Role role, Long userId, String userName) {
        return new LoginResult(true, "Login successful", role, userId, userName);
    }
}

