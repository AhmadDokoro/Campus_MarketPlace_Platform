package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import com.ahsmart.campusmarket.service.authentication.AuthenticationService;
import com.ahsmart.campusmarket.model.enums.Role;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/")
    public String home(){
        return "templates/index";
    }



    @PostMapping("/signup")
    public String signUp(){
        return "templates/auth/signup2";
    }

    @GetMapping("/signupSecondStep")
    public String signSecondStep(){
        return "templates/auth/signupstep1";
    }

    /**
     * Handle POST from login form. Receives username and password from the login.html form,
     * delegates authentication to AuthenticationService.userLogin, and acts on the result:
     * - on success: create a session attribute (userId and role) and redirect based on role
     *   BUYER -> index, SELLER -> seller dashboard, ADMIN -> admin dashboard
     * - if seller not yet verified -> redirect to seller review pending page
     * - on error -> re-render login view with an error message placed in the model (key: "error")
     */
    @PostMapping("/signin")
    public String signin(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password,
            Model model,
            HttpServletRequest httpServletRequest) {
        // call the service to validate credentials
        LoginResult result = authenticationService.userLogin(username, password);

        if (result == null) {
            // defensive: if service returned null, show generic error
            model.addAttribute("error", "Authentication service unavailable");
            return "templates/auth/login";
        }

        if (!result.isSuccess()) {
            // failed login: check specific message for seller-not-verified
            String msg = result.getMessage();
            if (msg != null && msg.toLowerCase().contains("seller account not yet verified")) {
                // redirect sellers who are not yet verified to a review pending page
                return "templates/seller/reviewPendingPage";
            }

            // otherwise re-show login page with error message under the form
            model.addAttribute("error", msg != null ? msg : "Login failed");
            return "templates/auth/login";
        }

        // success: create a session and redirect by role
        Role role = result.getRole();
        Long userId = result.getUserId();

        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute("user", result); // store the entire LoginResult object


        // route by role
        if (role == Role.BUYER) {
            return "redirect:/"; // index
        } else if (role == Role.SELLER) {
            return "redirect:/seller/dashboard.html"; // seller dashboard
        } else if (role == Role.ADMIN) {
            return "/templates/admin/dashboard"; // admin dashboard
        }

        // default fallback
        return "redirect:/";
    }
}
