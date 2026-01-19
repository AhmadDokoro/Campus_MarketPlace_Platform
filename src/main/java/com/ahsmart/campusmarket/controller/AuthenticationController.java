package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Mentor;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import com.ahsmart.campusmarket.service.authentication.AuthenticationService;
import com.ahsmart.campusmarket.service.mentor.MentorService;
import com.ahsmart.campusmarket.repositories.SellerRepository;
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

    // landing page
    @GetMapping("/")
    public String home() {
        return "splash"; // return index template
    }

    // Provide a direct mapping for "/index" so the splash page can redirect to it.
    // We intentionally keep this simple: it returns the Thymeleaf `index` template located
    // at src/main/resources/templates/index.html. The splash page's JS navigates to 'index'
    // (a relative URL) which resolves to /index; without this mapping Spring returns 404.
    @GetMapping("/index")
    public String indexPage() {
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
    public String requestVerificationForm(HttpSession session, Model model) {
        // If pendingSellerUserId is not in session but the user is already authenticated (userId present),
        // use it as the pending seller id so the form can be submitted without forcing a re-register.
        Object pendingObj = session.getAttribute("pendingSellerUserId");
        if (pendingObj == null) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                // copy authenticated user id into pending flow so POST has an id to work with
                session.setAttribute("pendingSellerUserId", userIdObj);
                session.setAttribute("pendingSeller", true);
            }
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

            // Special handling for unverified sellers
            if ("Seller account not yet verified".equalsIgnoreCase(result.getMessage())) {
                return "seller/reviewPendingPage"; // show pending review page
            }

            // If seller account was rejected, show a dedicated rejection page with persisted reason
            if ("Seller account rejected".equalsIgnoreCase(result.getMessage())) {
                // Clear any pending seller session flags to avoid confusion
                session.removeAttribute("pendingSeller");
                session.removeAttribute("pendingSellerUserId");

                // Try to load the stored rejection reason (best-effort)
                authenticationService.findUserByEmail(email).ifPresent(u -> {
                    sellerRepository.findByUser(u).ifPresent(s -> model.addAttribute("rejectionReason", s.getRejectionReason()));
                });

                return "seller/rejectApprove";
            }

            // If seller has no profile yet, show a helpful page with CTA to request verification
            if ("Seller profile not found".equalsIgnoreCase(result.getMessage())) {
                // Ensure the pending seller flow is recorded in session so the upload page knows who the user is.
                // Find user by email and set session pending attributes so requestVerification can proceed.
                authenticationService.findUserByEmail(email).ifPresent(u -> {
                    session.setAttribute("pendingSellerUserId", u.getUserId()); // pending id for upload
                    session.setAttribute("pendingSeller", true); // flag for pending seller
                });
                return "seller/noSellerProfile"; // page instructing user to request verification
            }

            model.addAttribute("error", result.getMessage());
            return "auth/login";
        }

        // Store minimal user info in session
        session.setAttribute("userId", result.getUserId()); // always store userId
        session.setAttribute("userName", result.getName()); // store name for UI greeting

        // Important: only set ROLE in session for non-seller accounts here.
        // For SELLER role we rely on AuthenticationService.userLogin to only succeed for APPROVED sellers.
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
                               Model model,
                               HttpSession session) {
        try {
            Users saved = authenticationService.registerUser(userForm); // save user to DB

            // Redirect based on role
            if (saved.getRole() == Role.SELLER) {
                // IMPORTANT: Do NOT grant full SELLER role in session until admin approval.
                // Instead, mark a pending seller flow in session so user can continue to upload verification docs.
                session.setAttribute("pendingSellerUserId", saved.getUserId()); // store pending user id
                session.setAttribute("pendingSeller", true); // flag that this user is in pending flow
                // do not set session role to SELLER here
                return "redirect:/auth/requestVerification"; // go to upload page
            }

            // For buyers: redirect to signin (do not auto-login)
            return "redirect:/signin";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("userForm", userForm);
            // repopulate mentors on validation error so the dropdown doesn't break
            model.addAttribute("mentors", mentorService.getAllMentors());
            return "auth/usersRegister"; // show registration form with error
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
        // ensure user is logged in in the pending flow; fall back to authenticated userId if available
        Object userIdObj = session.getAttribute("pendingSellerUserId"); // check pending user id
        if (userIdObj == null) {
            // fallback: maybe user is authenticated (userId in session) but pending flag wasn't set
            userIdObj = session.getAttribute("userId");
        }
        if (userIdObj == null) {
            // still missing → redirect to register so they can create an account
            return "redirect:/registerUser";
        }

        Long userId = (Long) userIdObj; // cast saved user id (from pending or authenticated session)

        // Server-side file size validation (limit per-file to 5MB as configured)
        long maxBytes = 5L * 1024L * 1024L; // 5MB
        if (idCardFile == null || idCardFile.isEmpty()) {
            model.addAttribute("error", "ID card image is required.");
            return "auth/requestVerification"; // show form with error
        }
        if (mynemoFile == null || mynemoFile.isEmpty()) {
            model.addAttribute("error", "Mynemo profile image is required.");
            return "auth/requestVerification"; // show form with error
        }
        // Validate sizes before attempting upload to avoid Tomcat MaxUploadSize exceptions
        if (idCardFile.getSize() > maxBytes || mynemoFile.getSize() > maxBytes) {
            model.addAttribute("error", "Each file must be 5MB or smaller. Please resize and try again.");
            return "auth/requestVerification"; // feedback to user
        }

        try {
            // request verification using service (uploads images inside the service)
            authenticationService.requestVerification(userId, idCardFile, mynemoFile);

            // on success clear pending flags from session (user will wait for admin)
            session.removeAttribute("pendingSeller");
            session.removeAttribute("pendingSellerUserId");

            // on success show a friendly pending review page
            return "seller/reviewPendingPage";

        } catch (IllegalArgumentException ex) {
            // add enhanced error list to model for display
            model.addAttribute("error", ex.getMessage());
            return "auth/requestVerification"; // show form with error message
        }
    }

}
