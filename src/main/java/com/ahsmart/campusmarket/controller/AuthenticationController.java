package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Users;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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



    // Display registration form
    @GetMapping("/registerUser")
    public String registerForm(Model model) {
        model.addAttribute("userForm", new Users());
        return "auth/usersRegister";
    }

    // Display registration form for seller step2 (now request verification)
    @GetMapping("/auth/requestVerification")
    public String requestVerificationForm() {
        return "auth/requestVerification";
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
                return "seller/reviewPendingPage"; // show pending review page
            }

            // If seller has no profile yet, show a helpful page with CTA to request verification
            if ("Seller profile not found".equalsIgnoreCase(result.getMessage())) {
                return "seller/noSellerProfile"; // page instructing user to request verification
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


        if (role == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }

        // Fallback redirect
        return "redirect:/";
    }

    // New: Handle logout - invalidate the HTTP session and redirect to home
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }




    // Handle registration submission
    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("userForm") Users userForm,
                               Model model,
                               HttpSession session) {
        try {
            Users saved = authenticationService.registerUser(userForm);

            // Redirect based on role
            if (saved.getRole() == Role.SELLER) {
                // For sellers store user id in session so step2 can reference it, then redirect
                session.setAttribute("userId", saved.getUserId());
                session.setAttribute("role", saved.getRole());
                return "redirect:/auth/requestVerification";
            }

            // For buyers: redirect to signin (do not auto-login)
            return "redirect:/signin";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("userForm", userForm);
            return "auth/usersRegister";
        }
    }



    // Handle seller request verification submission: upload two images and create Seller row
    @PostMapping("/auth/requestVerification")
     public String requestVerification(
            @RequestPart("idCardFile") MultipartFile idCardFile, // id card file
            @RequestPart("mynemoFile") MultipartFile mynemoFile, // mynemo profile file
            Model model,
            HttpSession session
    ) {
        // ensure user is logged in
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            // not logged in → redirect to user register page
            return "redirect:/registerUser";
        }

        Long userId = (Long) userIdObj;

        try {
            // request verification using service
            authenticationService.requestVerification(userId, idCardFile, mynemoFile);
            // on success redirect to pending review page
            return "/seller/reviewPendingPage";

        } catch (IllegalArgumentException ex) {
            // add enhanced error list to model for display
            model.addAttribute("error", ex.getMessage());
            return "auth/requestVerification";
        }
     }

 }
