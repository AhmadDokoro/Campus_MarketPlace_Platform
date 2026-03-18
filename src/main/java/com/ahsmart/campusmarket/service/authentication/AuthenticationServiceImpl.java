package com.ahsmart.campusmarket.service.authentication;

import com.ahsmart.campusmarket.model.Mentor;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import com.ahsmart.campusmarket.repositories.MentorRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.product.FileService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class  AuthenticationServiceImpl implements AuthenticationService {

    // Repository for user authentication data
    private final UsersRepository usersRepository;

    // Repository for seller verification data
    private final SellerRepository sellerRepository;

    // Mentor repository for mentor selection during registration
    private final MentorRepository mentorRepository;

    // Service to upload files (Cloudinary)
    private final FileService fileService;

    // Constructor injection
    public AuthenticationServiceImpl(
            UsersRepository usersRepository,
            SellerRepository sellerRepository,
            MentorRepository mentorRepository,
            FileService fileService
    ) {
        this.usersRepository = usersRepository;
        this.sellerRepository = sellerRepository;
        this.mentorRepository = mentorRepository;
        this.fileService = fileService;
    }




    @Override
    public LoginResult userLogin(String email, String submittedPassword) {

        // Fetch user by email
        Optional<Users> optionalUser = usersRepository.findByEmail(email);

        // Fail if user does not exist
        if (optionalUser.isEmpty()) {
            return LoginResult.failed("Invalid email or password");
        }

        Users user = optionalUser.get();

        // Validate password plain text for now
        if (!user.getPassword().equals(submittedPassword)) {
            return LoginResult.failed("Invalid email or password");
        }


        // Successful authentication result
        return LoginResult.success(
                user.getRole(),
                user.getUserId(),
                user.getFirstName()
        );
    }

    @Override
    public Optional<Users> findUserByEmail(String email) {
        // Delegate to UsersRepository to find user by email
        return usersRepository.findByEmail(email);
    }


    @Override
    public Users registerUser(Users user) throws IllegalArgumentException {
        // Basic validations
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getAcademicId() == null || user.getAcademicId().isBlank()) {
            throw new IllegalArgumentException("Academic ID is required");
        }

        // Uniqueness checks
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (user.getAcademicId() != null && usersRepository.findByAcademicId(user.getAcademicId()).isPresent()) {
            throw new IllegalArgumentException("Academic ID already registered");
        }

        // If mentorId selected from UI, resolve it to Mentor (mentor selection is nullable)
        if (user.getMentorId() != null) {
            Mentor mentor = mentorRepository.findById(user.getMentorId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected mentor not found"));
            user.setMentor(mentor);
        } else {
            user.setMentor(null);
        }
        // Avoid accidentally persisting mentorId anywhere else
        user.setMentorId(null);

        // Set defaults
        // Registration no longer supports picking SELLER; everyone starts as a BUYER.
        user.setRole(Role.BUYER);
        user.setCreatedAt(LocalDateTime.now());

        // Save user (no password hashing per request)
        return usersRepository.save(user);
    }



    @Override
    @Transactional
    public Seller requestVerification(Long userId, MultipartFile idCardFile, MultipartFile mynemoFile) throws IllegalArgumentException {

        // Validate user id provided
        if (userId == null) throw new IllegalArgumentException("User session missing. Please sign in or register.");
        // Lookup user by id
        Optional<Users> optionalUser = usersRepository.findById(userId); // find user


        // If user not found redirect to user registration
        if (optionalUser.isEmpty()) throw new IllegalArgumentException("User not found. Please complete user registration first.");
        // Get user entity
        Users user = optionalUser.get(); // user entity

        // Buyer-first flow: user can request verification even if currently BUYER.
        // If a seller profile exists:
        // - PENDING/APPROVED => block duplicate submissions
        // - REJECTED => allow resubmission by replacing docs and setting status back to PENDING
        Optional<Seller> existingOpt = sellerRepository.findByUser(user);
        if (existingOpt.isPresent()) {
            Seller existing = existingOpt.get();
            if (existing.getStatus() == SellerStatus.PENDING || existing.getStatus() == SellerStatus.APPROVED) {
                throw new IllegalArgumentException("Seller profile already submitted and under review.");
            }
            // REJECTED flows into update/resubmission below.
        }

        // Validate files
        if (idCardFile == null || idCardFile.isEmpty()) {
            throw new IllegalArgumentException("ID card image is required.");
        }
        if (mynemoFile == null || mynemoFile.isEmpty()) {
            throw new IllegalArgumentException("Mynemo profile image is required.");
        }

        try {
            // Upload id card image to cloudinary and get URL
            String idCardUrl = fileService.uploadImage(idCardFile); // upload id card
            // Upload mynemo profile image to cloudinary and get URL
            String mynemoUrl = fileService.uploadImage(mynemoFile); // upload mynemo profile

            Seller seller;
            if (existingOpt.isPresent() && existingOpt.get().getStatus() == SellerStatus.REJECTED) {
                // Resubmission: update existing seller record
                seller = existingOpt.get();
                seller.setIdCardImageUrl(idCardUrl);
                seller.setMynemoProfileUrl(mynemoUrl);
                seller.setStatus(SellerStatus.PENDING);
                seller.setRejectionReason(null);
            } else {
                // New submission
                seller = new Seller();
                seller.setUser(user); // associate user
                seller.setIdCardImageUrl(idCardUrl);
                seller.setMynemoProfileUrl(mynemoUrl);
                seller.setStatus(SellerStatus.PENDING);
            }

            // Mark account as SELLER once they request seller verification.
            // They still can't access seller features until approved (handled elsewhere).
            if (user.getRole() != Role.SELLER) {
                user.setRole(Role.SELLER);
                usersRepository.save(user);
            }

            // Save seller to DB
            return sellerRepository.save(seller);

        } catch (DataIntegrityViolationException dive) {
            // translate DB uniqueness violation
            throw new IllegalArgumentException("Seller profile could not be created: data conflict.");
        } catch (Exception e) {
            // On any failure, wrap and report
            throw new IllegalArgumentException("Failed to upload images or save seller: " + e.getMessage());
        }
    }

    @Override
    public boolean isSellerApproved(Long userId) {
        // Look up user by id and determine if there's an approved seller record
        Optional<Users> opt = usersRepository.findById(userId); // find user
        if (opt.isEmpty()) return false; // no user -> not approved
        Users user = opt.get();
        // Check repository for seller and ensure status == APPROVED
        Optional<Seller> sellerOpt = sellerRepository.findByUser(user);
        return sellerOpt.isPresent() && sellerOpt.get().getStatus() == SellerStatus.APPROVED; // true only when approved
    }

}
