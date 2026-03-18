package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCategoryControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void categories_notAdmin_redirectsToSignin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", Role.BUYER);

        mockMvc.perform(get("/admin/categories").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }

    @Test
    void categories_admin_loadsPage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", Role.ADMIN);
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/admin/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"));
    }
}

