package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Mentor;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.payloadDTOs.admin.WeeklyListingDTO;
import com.ahsmart.campusmarket.service.admin.AdminService;
import com.ahsmart.campusmarket.service.mentor.MentorService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class); // logger

    @Autowired
    private AdminService adminService; // admin service

    @Autowired
    private MentorService mentorService;

    private boolean isAdmin(HttpSession session) {
        Object roleObj = session.getAttribute("role");
        return (roleObj instanceof Role) && Role.ADMIN.equals(roleObj);
    }

    // Show admin dashboard with live analytics data
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        model.addAttribute("totalUsers",     adminService.getTotalUsers());
        model.addAttribute("totalSellers",   adminService.getTotalSellers());
        model.addAttribute("verifiedSellers",adminService.getVerifiedSellers());
        model.addAttribute("activeListings", adminService.getActiveListings());
        model.addAttribute("totalSales",     adminService.getTotalSales());

        // Split weekly listings into parallel label + count arrays for Chart.js
        List<WeeklyListingDTO> weekly = adminService.getWeeklyListings();
        List<String> weeklyLabels = weekly.stream().map(WeeklyListingDTO::getWeekLabel).toList();
        List<Long>   weeklyCounts = weekly.stream().map(WeeklyListingDTO::getCount).toList();
        model.addAttribute("weeklyLabels", weeklyLabels);
        model.addAttribute("weeklyCounts", weeklyCounts);

        Map<String, Long> vStats = adminService.getVerificationStatusStats();
        model.addAttribute("verificationPending",  vStats.getOrDefault("PENDING",  0L));
        model.addAttribute("verificationApproved", vStats.getOrDefault("APPROVED", 0L));
        model.addAttribute("verificationRejected", vStats.getOrDefault("REJECTED", 0L));

        model.addAttribute("topCategories", adminService.getTopCategories());
        model.addAttribute("sellerStats",   adminService.getSellerStats());

        return "admin/dashboard";
    }

    // Show a page with pending sellers
    @GetMapping("/pendingSellers")
    public String pendingSellersPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        try {
            List<Seller> sellers = adminService.getPendingSellers();
            model.addAttribute("sellers", sellers);
        } catch (Exception ex) {
            logger.error("Failed to load pending sellers", ex);
            model.addAttribute("error", "Unable to load pending sellers: " + ex.getMessage());
            model.addAttribute("sellers", java.util.Collections.emptyList());
        }
        return "admin/pendingSellerList";
    }

    // Return details for a seller (AJAX or server-side render)
    @GetMapping("/getSellerDetailsForReview")
    public String getSellerDetailsForReview(@RequestParam("sellerId") Long sellerId, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        Seller seller = adminService.getSellerForReview(sellerId);
        model.addAttribute("seller", seller);
        return "admin/admin-seller-verification";
    }

    // Approve seller
    @PostMapping("/seller/{id}/approve")
    public String approveSeller(@PathVariable("id") Long sellerId, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        // get reviewer id from session
        Object userIdObj = session.getAttribute("userId");
        Long reviewerId = userIdObj == null ? null : (Long) userIdObj;
        // perform review (also clears rejection reason)
        adminService.reviewSeller(sellerId, SellerStatus.APPROVED, reviewerId);
        return "redirect:/admin/pendingSellers";
    }

    // -------- Reject flow with required reason --------

    // Show reject reason form
    @GetMapping("/seller/{id}/rejectReason")
    public String showRejectReasonForm(@PathVariable("id") Long sellerId, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        Seller seller = adminService.getSellerForReview(sellerId);
        model.addAttribute("seller", seller);
        return "admin/rejectReason";
    }

    // Submit rejection with reason
    @PostMapping("/seller/{id}/reject")
    public String rejectSeller(@PathVariable("id") Long sellerId,
                               @RequestParam("rejectionReason") String rejectionReason,
                               Model model,
                               HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        Object userIdObj = session.getAttribute("userId");
        Long reviewerId = userIdObj == null ? null : (Long) userIdObj;

        try {
            adminService.rejectSeller(sellerId, reviewerId, rejectionReason);
            return "redirect:/admin/pendingSellers";
        } catch (IllegalArgumentException ex) {
            // keep the seller context so the form can re-render with error
            Seller seller = adminService.getSellerForReview(sellerId);
            model.addAttribute("seller", seller);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("rejectionReason", rejectionReason);
            return "admin/rejectReason";
        }
    }

    // ------------------- Manage Mentors -------------------

    @GetMapping("/mentors")
    public String manageMentors(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        List<Mentor> mentors = mentorService.getAllMentors();
        model.addAttribute("mentors", mentors);
        return "admin/manageMentors";
    }

    @PostMapping("/mentors")
    public String addMentor(@RequestParam("mentorName") String mentorName,
                            @RequestParam("mentorEmail") String mentorEmail,
                            Model model,
                            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        try {
            mentorService.addMentor(mentorName, mentorEmail);
            return "redirect:/admin/mentors";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("mentors", mentorService.getAllMentors());
            return "admin/manageMentors";
        }
    }

    @PostMapping("/mentors/{id}/delete")
    public String deleteMentor(@PathVariable("id") Long mentorId,
                               Model model,
                               HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        try {
            mentorService.deleteMentor(mentorId);
            return "redirect:/admin/mentors";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("mentors", mentorService.getAllMentors());
            return "admin/manageMentors";
        }
    }
}
