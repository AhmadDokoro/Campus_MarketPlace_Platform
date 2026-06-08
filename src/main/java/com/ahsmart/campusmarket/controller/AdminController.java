package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Mentor;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.payloadDTOs.admin.WeeklyListingDTO;
import com.ahsmart.campusmarket.service.admin.AdminService;
import com.ahsmart.campusmarket.service.embedding.EmbeddingService;
import com.ahsmart.campusmarket.service.mentor.MentorService;
import com.ahsmart.campusmarket.service.report.AdminReportData;
import com.ahsmart.campusmarket.service.report.AdminReportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@lombok.RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class); // logger

    private final AdminService adminService;
    private final MentorService mentorService;
    private final EmbeddingService embeddingService;
    private final AdminReportService adminReportService;

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
        model.addAttribute("flaggedListings",adminService.getSuspiciousProducts().size());
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
        model.addAttribute("reportYears", adminReportService.getSelectableYears());
        model.addAttribute("reportYearSelection",
                model.containsAttribute("reportYearSelection") ? model.asMap().get("reportYearSelection") : LocalDate.now().getYear());
        model.addAttribute("reportMonthSelection",
                model.containsAttribute("reportMonthSelection") ? model.asMap().get("reportMonthSelection") : null);
        model.addAttribute("openReportModal",
                model.containsAttribute("openReportModal") && Boolean.TRUE.equals(model.asMap().get("openReportModal")));

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
    public String approveSeller(@PathVariable("id") Long sellerId, HttpSession session, RedirectAttributes redirectAttrs) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        Object userIdObj = session.getAttribute("userId");
        Long reviewerId = userIdObj == null ? null : (Long) userIdObj;
        Seller approved = adminService.reviewSeller(sellerId, SellerStatus.APPROVED, reviewerId);
        String sellerName = approved.getUser() != null
                ? approved.getUser().getFirstName() + " " + approved.getUser().getLastName()
                : "Seller";
        redirectAttrs.addFlashAttribute("approvedMessage", sellerName + " has been approved as a seller.");
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

    // ------------------- Flagged Products -------------------

    @GetMapping("/flaggedProducts")
    public String flaggedProducts(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        model.addAttribute("flaggedProducts", adminService.getSuspiciousProducts());
        return "admin/flaggedProducts";
    }

    @PostMapping("/flaggedProducts/{id}/approve")
    public String approveProduct(@PathVariable("id") Long productId, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        adminService.approveProduct(productId);
        return "redirect:/admin/flaggedProducts";
    }

    @PostMapping("/flaggedProducts/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long productId, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        adminService.adminDeleteProduct(productId);
        return "redirect:/admin/flaggedProducts";
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

    // ------------------- Embedding Backfill -------------------

    @PostMapping("/refreshEmbeddings")
    public String refreshEmbeddings(HttpSession session,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        try {
            int updated = embeddingService.backfillMissingEmbeddings();
            redirectAttributes.addFlashAttribute("embeddingSuccess",
                    "Embeddings refreshed successfully. " + updated + " product(s) updated.");
        } catch (Exception e) {
            logger.error("Embedding backfill failed", e);
            redirectAttributes.addFlashAttribute("embeddingError",
                    "Embedding refresh failed: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // ------------------- Report Preview + PDF Export -------------------

    @GetMapping("/report")
    public String reportPreview(@RequestParam(value = "year", required = false) Integer year,
                                @RequestParam(value = "month", required = false) Integer month,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }
        try {
            AdminReportData reportData = adminReportService.buildReport(year, month);
            model.addAttribute("reportData", reportData);
            return "admin/report-preview";
        } catch (IllegalArgumentException ex) {
            redirectReportError(redirectAttributes, ex.getMessage(), year, month);
            return "redirect:/admin/dashboard";
        } catch (Exception ex) {
            logger.error("Report preview generation failed", ex);
            redirectReportError(redirectAttributes, "Unable to generate the report right now. Please try again.", year, month);
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/report/export")
    public void exportReport(@RequestParam(value = "year", required = false) Integer year,
                             @RequestParam(value = "month", required = false) Integer month,
                             HttpSession session,
                             HttpServletResponse response) {
        if (!isAdmin(session)) {
            try { response.sendRedirect("/signin"); } catch (Exception ignored) {}
            return;
        }
        try {
            AdminReportData reportData = adminReportService.buildReport(year, month);
            byte[] pdf = adminReportService.generateReportPdf(reportData);
            String filename = adminReportService.buildFilename(reportData.period());
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (IllegalArgumentException ex) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            } catch (Exception ignored) {}
        } catch (Exception e) {
            logger.error("Report export failed", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report export failed"); }
            catch (Exception ignored) {}
        }
    }

    private void redirectReportError(RedirectAttributes redirectAttributes, String message, Integer year, Integer month) {
        redirectAttributes.addFlashAttribute("reportFilterError", message);
        redirectAttributes.addFlashAttribute("reportYearSelection", year);
        redirectAttributes.addFlashAttribute("reportMonthSelection", month);
        redirectAttributes.addFlashAttribute("openReportModal", true);
    }
}
