package com.ahsmart.campusmarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestVerificationPostGuardTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void postRequestVerification_notLoggedIn_redirectsToSignin() throws Exception {
        MockMultipartFile idCard = new MockMultipartFile("idCardFile", "id.png", "image/png", "x".getBytes());
        MockMultipartFile mynemo = new MockMultipartFile("mynemoFile", "m.png", "image/png", "y".getBytes());

        mockMvc.perform(multipart("/auth/requestVerification")
                        .file(idCard)
                        .file(mynemo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }
}

