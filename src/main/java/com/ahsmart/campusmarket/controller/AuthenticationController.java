package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import com.ahsmart.campusmarket.service.authentication.AuthenticationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    // Authentication business logic service
    @Autowired
    private AuthenticationService authenticationService;

    // landing page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Display login page
    @GetMapping("/signin")
    public String signinPage() {
        return "auth/login";
    }

    // Handle login form submission
    @PostMapping("/signin")
    public String signin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session
    ) {

        // Delegate authentication to service
        LoginResult result = authenticationService.userLogin(email, password);

        // Handle failed authentication
        if (!result.isSuccess()) {

            // Special handling for unverified sellers
            if ("Seller account not yet verified".equalsIgnoreCase(result.getMessage())) {
                return "seller/reviewPendingPage";
            }

            model.addAttribute("error", result.getMessage());
            return "auth/login";
        }

        // Store minimal user info in session
        session.setAttribute("userId", result.getUserId());
        session.setAttribute("userName", result.getName());
        session.setAttribute("role", result.getRole());

        // Redirect user based on role
        Role role = result.getRole();

        if (role == Role.BUYER) {
            return "redirect:/";
        }
        if (role == Role.SELLER) {
            return "redirect:/seller/dashboard";
        }
        if (role == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }

        // Fallback redirect
        return "redirect:/";
    }
}
