package com.ahsmart.campusmarket.service.authentication;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    // Repository for user authentication data
    private final UsersRepository usersRepository;

    // Repository for seller verification data
    private final SellerRepository sellerRepository;

    // Constructor injection
    public AuthenticationServiceImpl(
            UsersRepository usersRepository,
            SellerRepository sellerRepository
    ) {
        this.usersRepository = usersRepository;
        this.sellerRepository = sellerRepository;
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

        // Additional verification check for sellers
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
        }

        // Successful authentication result
        return LoginResult.success(
                user.getRole(),
                user.getUserId(),
                user.getFirstName()
        );
    }
}
