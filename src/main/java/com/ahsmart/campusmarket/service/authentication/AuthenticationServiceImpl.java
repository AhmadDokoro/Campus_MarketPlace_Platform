package com.ahsmart.campusmarket.service.authentication;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
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

    // Service to upload files (Cloudinary)
    private final FileService fileService;

    // Constructor injection
    public AuthenticationServiceImpl(
            UsersRepository usersRepository,
            SellerRepository sellerRepository,
            FileService fileService
    ) {
        this.usersRepository = usersRepository;
        this.sellerRepository = sellerRepository;
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

        // Additional verification check for sellers: use helper isSellerApproved
        if (user.getRole() == Role.SELLER) {

            Optional<Seller> optionalSeller = sellerRepository.findByUser(user);

            // Seller profile must exist
            if (optionalSeller.isEmpty()) {
                return LoginResult.failed("Seller profile not found");
            }

            Seller seller = optionalSeller.get();

            // Seller must be approved by admin
            if (seller.getStatus() == SellerStatus.PENDING) {
                return LoginResult.failed("Seller account not yet verified");
            }
            // If seller was explicitly rejected, return a clear failure message so UI can show rejection flow
            if (seller.getStatus() == SellerStatus.REJECTED) {
                return LoginResult.failed("Seller account rejected");
            }
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

        // Set defaults
        if (user.getRole() == null) {
            user.setRole(Role.BUYER);
        }
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


        // Ensure user role is SELLER (or has selected seller role)
        if (user.getRole() != Role.SELLER) {
            // if user hasn't selected seller role, instruct to choose seller
            throw new IllegalArgumentException("Your account is not set as a seller. Please choose Seller role during registration.");
        }

        // Check if seller profile already exists for this user
        if (sellerRepository.findByUser(user).isPresent()) {
            // already submitted
            throw new IllegalArgumentException("Seller profile already submitted and under review.");
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

            // Build seller entity
            Seller seller = new Seller(); // create seller object
            seller.setUser(user); // associate user
            seller.setIdCardImageUrl(idCardUrl); // set id card url
            seller.setMynemoProfileUrl(mynemoUrl); // set mynemo url
            seller.setStatus(SellerStatus.PENDING); // set pending status

            // Save seller to DB
            return sellerRepository.save(seller); // persist and return seller

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
