package com.ahsmart.campusmarket.controller.advice;

import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.admin.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects shared model attributes (nav count badges + admin identity) into every
 * page rendered by the two admin controllers. Scoped to AdminController and
 * CategoryController so non-admin pages are untouched and the queries only run
 * for admin views.
 */
@ControllerAdvice(assignableTypes = {
        com.ahsmart.campusmarket.controller.AdminController.class,
        com.ahsmart.campusmarket.controller.CategoryController.class
})
public class AdminGlobalModelAdvice {

    private final AdminService adminService;
    private final UsersRepository usersRepository;

    public AdminGlobalModelAdvice(AdminService adminService, UsersRepository usersRepository) {
        this.adminService = adminService;
        this.usersRepository = usersRepository;
    }

    private boolean isAdmin(HttpSession session) {
        Object roleObj = session.getAttribute("role");
        return (roleObj instanceof Role) && Role.ADMIN.equals(roleObj);
    }

    /** Number of products currently flagged as suspicious (nav badge + dashboard stat). */
    @ModelAttribute("flaggedListings")
    public int flaggedListings(HttpSession session) {
        if (!isAdmin(session)) {
            return 0;
        }
        return adminService.getSuspiciousProducts().size();
    }

    /** Sellers awaiting verification (nav badge + dashboard chart). */
    @ModelAttribute("verificationPending")
    public long verificationPending(HttpSession session) {
        if (!isAdmin(session)) {
            return 0L;
        }
        return adminService.getVerificationStatusStats().getOrDefault("PENDING", 0L);
    }

    /** Full name of the logged-in admin, trimmed; falls back to "Admin". */
    @ModelAttribute("adminName")
    public String adminName(HttpSession session) {
        Users admin = loadAdmin(session);
        if (admin == null) {
            return "Admin";
        }
        String first = admin.getFirstName() == null ? "" : admin.getFirstName();
        String last = admin.getLastName() == null ? "" : admin.getLastName();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Admin" : full;
    }

    /** Uppercase first initial of the admin's first name; falls back to "A". */
    @ModelAttribute("adminInitial")
    public String adminInitial(HttpSession session) {
        Users admin = loadAdmin(session);
        if (admin == null || admin.getFirstName() == null || admin.getFirstName().isBlank()) {
            return "A";
        }
        return admin.getFirstName().trim().substring(0, 1).toUpperCase();
    }

    /** Static role label shown in the topbar. */
    @ModelAttribute("adminRole")
    public String adminRole(HttpSession session) {
        return "Administrator";
    }

    /** Loads the logged-in admin Users entity, or null if not an admin / no session id. */
    private Users loadAdmin(HttpSession session) {
        if (!isAdmin(session)) {
            return null;
        }
        Object userIdObj = session.getAttribute("userId");
        if (!(userIdObj instanceof Long userId)) {
            return null;
        }
        return usersRepository.findById(userId).orElse(null);
    }
}
