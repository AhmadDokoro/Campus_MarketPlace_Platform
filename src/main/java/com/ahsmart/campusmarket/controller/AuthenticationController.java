package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Mentor;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.service.authentication.AuthenticationService;
import com.ahsmart.campusmarket.service.mentor.MentorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class AuthenticationController {

    // Authentication business logic service (constructor/field injection used)
    @Autowired
    private AuthenticationService authenticationService;

    // mentor service used to populate mentor dropdown during registration
    @Autowired
    private MentorService mentorService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private com.ahsmart.campusmarket.service.product.ProductService productService;

    @Autowired
    private com.ahsmart.campusmarket.service.category.CategoryService categoryService;

    // landing page
    @GetMapping("/")
    public String home() {
        return "splash"; // return index template
    }



    @GetMapping("/index")
    public String indexPage(Model model) {
        // Load top categories and featured products for the homepage.
        java.util.List<com.ahsmart.campusmarket.model.Category> topCategories = categoryService.getTopCategories(7);
        // Single GROUP BY query replaces the previous N per-category COUNT loop.
        java.util.Map<Long, Long> categoryCounts = productService.getCategoryCountMap();
        model.addAttribute("topCategories", topCategories);
        model.addAttribute("categoryCounts", categoryCounts);
        model.addAttribute("featuredProducts", productService.getFeaturedProducts(20));
        return "index"; // render the main index template after splash
    }


    // Display login page
    @GetMapping("/signin")
    public String signinPage() {
        return "auth/login"; // login template
    }


    // Display registration form
    @GetMapping("/registerUser")
    public String registerForm(Model model) {
        model.addAttribute("userForm", new Users()); // populate empty form model
        List<Mentor> mentors = mentorService.getAllMentors();
        model.addAttribute("mentors", mentors);
        return "auth/usersRegister";
    }


    // Display registration form for seller step2 (request verification)
    @GetMapping("/auth/requestVerification")
    public String requestVerificationForm(HttpSession session) {
        // Guard: must be logged in to request seller verification
        if (session.getAttribute("userId") == null) {
            return "redirect:/signin";
        }
        return "auth/requestVerification"; // render the upload form
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

            // Seller verification is now accessed from Profile → Start selling.
            // Keep error feedback simple here.
            model.addAttribute("error", result.getMessage());
            return "auth/login";
        }

        // Store minimal user info in session
        session.setAttribute("userId", result.getUserId()); // always store userId
        session.setAttribute("userName", result.getName()); // store name for UI greeting
        session.setAttribute("role", result.getRole());

        // Redirect user based on role  admin goes to admin dashboard
        Role role = result.getRole();

        if (role == Role.ADMIN) {
            return "redirect:/admin/dashboard"; // explicit redirect to controller mapping
        }

        // Fallback redirect to home
        return "redirect:/index";
    }

    // New: Handle logout - invalidate the HTTP session and redirect to home
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate(); // clear session on logout
        }
        return "redirect:/index"; // go home
    }

    // Handle registration submission
    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("userForm") Users userForm,
                               Model model) {
        try {
            authenticationService.registerUser(userForm); // save user to DB

            // Registration is buyer-only now: redirect to signin (do not auto-login)
            return "redirect:/signin";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("userForm", userForm);
            // repopulate mentors on validation error so the dropdown doesn't break
            model.addAttribute("mentors", mentorService.getAllMentors());
            return "auth/usersRegister"; // show registration form with error
        }
    }

    // Handle seller verification document submission
    @PostMapping("/auth/requestVerification")
    public String submitVerification(
            @RequestParam("idCardFile") MultipartFile idCardFile,
            @RequestParam("mynemoFile") MultipartFile mynemoFile,
            Model model,
            HttpSession session
    ) {
        // Guard: must be logged in
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }

        Long userId;
        if (userIdObj instanceof Long l) {
            userId = l;
        } else {
            userId = Long.valueOf(String.valueOf(userIdObj));
        }

        try {
            authenticationService.requestVerification(userId, idCardFile, mynemoFile);

            // Clear pending session flags if any
            session.removeAttribute("pendingSeller");
            session.removeAttribute("pendingSellerUserId");

            // After submission, always show the existing pending review page
            return "seller/reviewPendingPage";

        } catch (IllegalArgumentException ex) {
            // show error on the same page
            model.addAttribute("error", ex.getMessage());
            return "auth/requestVerification";
        }
    }

}
