package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
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
class UserControllerStartSellingTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Test
    void startSelling_notLoggedIn_redirectsToSignin() throws Exception {
        mockMvc.perform(get("/user/start-selling"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }

    @Test
    void startSelling_noSellerProfile_redirectsToRequestVerification_andSetsPendingSession() throws Exception {
        Users u = new Users();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("test.user@umt.edu");
        u.setPassword("pass");
        u.setAcademicId("ACD-100");
        u.setRole(Role.BUYER);
        Users saved = usersRepository.save(u);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", saved.getUserId());

        mockMvc.perform(get("/user/start-selling").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/requestVerification"))
                .andExpect(request().sessionAttribute("pendingSeller", true))
                .andExpect(request().sessionAttribute("pendingSellerUserId", saved.getUserId()));
    }

    @Test
    void startSelling_pendingSeller_showsReviewPendingPage() throws Exception {
        Users u = new Users();
        u.setFirstName("Pending");
        u.setLastName("Seller");
        u.setEmail("pending.seller@umt.edu");
        u.setPassword("pass");
        u.setAcademicId("ACD-101");
        u.setRole(Role.SELLER);
        Users saved = usersRepository.save(u);

        Seller s = new Seller();
        s.setUser(saved);
        s.setIdCardImageUrl("http://example.com/id.png");
        s.setMynemoProfileUrl("http://example.com/m.png");
        s.setStatus(SellerStatus.PENDING);
        sellerRepository.save(s);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", saved.getUserId());

        mockMvc.perform(get("/user/start-selling").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/reviewPendingPage"));
    }

    @Test
    void startSelling_rejectedSeller_showsRejectApprovePage_withReason() throws Exception {
        Users u = new Users();
        u.setFirstName("Rejected");
        u.setLastName("Seller");
        u.setEmail("rejected.seller@umt.edu");
        u.setPassword("pass");
        u.setAcademicId("ACD-103");
        u.setRole(Role.SELLER);
        Users saved = usersRepository.save(u);

        Seller s = new Seller();
        s.setUser(saved);
        s.setIdCardImageUrl("http://example.com/id.png");
        s.setMynemoProfileUrl("http://example.com/m.png");
        s.setStatus(SellerStatus.REJECTED);
        s.setRejectionReason("Blurry ID card");
        sellerRepository.save(s);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", saved.getUserId());

        mockMvc.perform(get("/user/start-selling").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/rejectApprove"))
                .andExpect(model().attribute("rejectionReason", "Blurry ID card"));
    }

    @Test
    void startSelling_approvedSeller_redirectsToSellerDashboard() throws Exception {
        Users u = new Users();
        u.setFirstName("Approved");
        u.setLastName("Seller");
        u.setEmail("approved.seller@umt.edu");
        u.setPassword("pass");
        u.setAcademicId("ACD-102");
        u.setRole(Role.SELLER);
        Users saved = usersRepository.save(u);

        Seller s = new Seller();
        s.setUser(saved);
        s.setIdCardImageUrl("http://example.com/id.png");
        s.setMynemoProfileUrl("http://example.com/m.png");
        s.setStatus(SellerStatus.APPROVED);
        sellerRepository.save(s);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", saved.getUserId());

        mockMvc.perform(get("/user/start-selling").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"));
    }
}

