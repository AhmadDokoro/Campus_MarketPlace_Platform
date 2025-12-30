package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.service.admin.AdminService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class); // logger

    @Autowired
    private AdminService adminService; // admin service

    // Show admin dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        // ensure only admins can view
        Object roleObj = session.getAttribute("role");
        if (!(roleObj instanceof Role) || !Role.ADMIN.equals(roleObj)) {
            return "redirect:/signin";
        }
        // optionally populate model with summary stats later
        return "admin/dashboard";
    }

    // Show a page with pending sellers
    @GetMapping("/pendingSellers")
    public String pendingSellersPage(Model model, HttpSession session) {
         // check role in session
        Object roleObj = session.getAttribute("role");
        if (!(roleObj instanceof Role) || !Role.ADMIN.equals(roleObj)) {
            return "redirect:/signin"; // not authorized
        }
        try {
            // fetch pending sellers and add to model
            List<Seller> sellers = adminService.getPendingSellers();
            model.addAttribute("sellers", sellers);
        } catch (Exception ex) {
            // log the exception with stacktrace for debugging
            logger.error("Failed to load pending sellers", ex);
            // on any error show a friendly message instead of throwing
            model.addAttribute("error", "Unable to load pending sellers: " + ex.getMessage());
            model.addAttribute("sellers", java.util.Collections.emptyList());
        }
        return "admin/pendingSellerList"; // render template
    }

    // Return details for a seller (AJAX or server-side render)
    @GetMapping("/getSellerDetailsForReview")
    public String getSellerDetailsForReview(@RequestParam("sellerId") Long sellerId, Model model, HttpSession session) {
        // session role check
        Object roleObj = session.getAttribute("role");
        if (!(roleObj instanceof Role) || !Role.ADMIN.equals(roleObj)) {
            return "redirect:/signin"; // not authorized
        }
        // fetch seller and add to model
        Seller seller = adminService.getSellerForReview(sellerId);
        model.addAttribute("seller", seller);
        return "admin/admin-seller-verification"; // existing template path
    }

    // Approve seller
    @PostMapping("/seller/{id}/approve")
    public String approveSeller(@PathVariable("id") Long sellerId, HttpSession session) {
        // get reviewer id from session
        Object userIdObj = session.getAttribute("userId");
        Long reviewerId = userIdObj == null ? null : (Long) userIdObj;
        // perform review
        adminService.reviewSeller(sellerId, SellerStatus.APPROVED, reviewerId);
        return "redirect:/admin/pendingSellers";
    }

    // Reject seller
    @PostMapping("/seller/{id}/reject")
    public String rejectSeller(@PathVariable("id") Long sellerId, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        Long reviewerId = userIdObj == null ? null : (Long) userIdObj;
        adminService.reviewSeller(sellerId, SellerStatus.REJECTED, reviewerId);
        return "redirect:/admin/pendingSellers";
    }
}
