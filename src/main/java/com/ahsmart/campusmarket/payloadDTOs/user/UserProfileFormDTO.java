package com.ahsmart.campusmarket.payloadDTOs.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileFormDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String academicId;
    private String level;
    private String hostelBlock;
    private String floor;
    private String roomNumber;
    private String city;
    private String state;

    public String getDisplayName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "User" : full;
    }
}
