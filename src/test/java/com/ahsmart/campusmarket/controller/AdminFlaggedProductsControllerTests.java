package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.service.admin.AdminService;
import com.ahsmart.campusmarket.service.mentor.MentorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminFlaggedProductsControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AdminService adminService;

    @MockBean
    MentorService mentorService;

    @Test
    void deleteFlaggedProduct_notAdmin_redirectsToSignin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", Role.BUYER);

        mockMvc.perform(post("/admin/flaggedProducts/5/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }

    @Test
    void deleteFlaggedProduct_success_redirectsWithSuccessFlash() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", Role.ADMIN);
        doNothing().when(adminService).adminDeleteProduct(5L);

        mockMvc.perform(post("/admin/flaggedProducts/5/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flaggedProducts"))
                .andExpect(flash().attribute("success", "Product deleted successfully."));
    }

    @Test
    void deleteFlaggedProduct_serviceValidationFailure_redirectsWithErrorFlash() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", Role.ADMIN);
        doThrow(new IllegalArgumentException("Cannot delete this product because it already appears in an order."))
                .when(adminService)
                .adminDeleteProduct(5L);

        mockMvc.perform(post("/admin/flaggedProducts/5/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flaggedProducts"))
                .andExpect(flash().attribute("error", "Cannot delete this product because it already appears in an order."));
    }
}
