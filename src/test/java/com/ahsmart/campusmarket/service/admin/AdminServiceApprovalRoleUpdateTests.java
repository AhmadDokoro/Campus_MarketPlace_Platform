package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.helper.EmailHelper;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AdminServiceApprovalRoleUpdateTests {

    @TestConfiguration
    static class StubConfig {
        @Bean
        public EmailHelper emailHelper() {
            // Simple stub to prevent real email sending in tests.
            return new EmailHelper() {
                @Override
                public void sendEmail(String to, String subject, String body) {
                    // no-op
                }
            };
        }
    }

    @Autowired
    AdminService adminService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Test
    void approvingSeller_updatesUserRoleToSeller() {
        Users u = new Users();
        u.setFirstName("Buyer");
        u.setLastName("ToSeller");
        u.setEmail("buyer.to.seller@umt.edu");
        u.setPassword("pass");
        u.setAcademicId("ACD-200");
        u.setRole(Role.BUYER);
        Users savedUser = usersRepository.save(u);

        Seller s = new Seller();
        s.setUser(savedUser);
        s.setIdCardImageUrl("http://example.com/id.png");
        s.setMynemoProfileUrl("http://example.com/m.png");
        s.setStatus(SellerStatus.PENDING);
        Seller savedSeller = sellerRepository.save(s);

        adminService.reviewSeller(savedSeller.getSellerId(), SellerStatus.APPROVED, null);

        Users refreshed = usersRepository.findById(savedUser.getUserId()).orElseThrow();
        assertEquals(Role.SELLER, refreshed.getRole());
    }
}
