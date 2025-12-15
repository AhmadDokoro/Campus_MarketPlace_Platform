package com.ahsmart.campusmarket.service.authentication;

import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;

public interface AuthenticationService {

    LoginResult userLogin(String email, String submittedPassword);

}

