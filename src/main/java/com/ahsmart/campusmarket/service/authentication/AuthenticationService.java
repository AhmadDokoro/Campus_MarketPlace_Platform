package com.ahsmart.campusmarket.service.authentication;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface AuthenticationService {

    LoginResult userLogin(String email, String submittedPassword);

    // Register a new user (returns saved Users on success, throws IllegalArgumentException on validation failure)
    Users registerUser(Users user) throws IllegalArgumentException;

    // Request seller verification: upload id-card + mynemo profile images and create Seller entity
    Seller requestVerification(Long userId, MultipartFile idCardFile, MultipartFile mynemoFile) throws IllegalArgumentException;

    // Check if a user (who selected seller role) has an approved seller profile
    boolean isSellerApproved(Long userId);

    // Find a user by email (useful for sign-in flows that need to set session state when seller profile is missing)
    Optional<Users> findUserByEmail(String email);

}
