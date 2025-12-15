package com.ahsmart.campusmarket.service.authentication;


import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import org.springframework.stereotype.Service;


import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.payloadDTOs.AuthenticationDTOs.LoginResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;

    // constructor injection
    public AuthenticationServiceImpl(
            UsersRepository usersRepository,
            SellerRepository sellerRepository
    ) {
        this.usersRepository = usersRepository;
        this.sellerRepository = sellerRepository;
    }


    @Override
    public LoginResult userLogin(String email, String submittedPassword) {
        // get the user as optional type
        Optional<Users> optionalUser = usersRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return LoginResult.failed("Invalid email or password");
        }

        // convert to users type from optional
        Users user = optionalUser.get();

        // verify with password
        if (!user.getPassword().equals(submittedPassword)) {
            return LoginResult.failed("Invalid email or password");
        }

        // If user is a seller, check verification status
        if (user.getRole() == Role.SELLER) {

            Optional<Seller> optionalSeller =
                    sellerRepository.findByUser(user);

            if (optionalSeller.isEmpty()) {
                return LoginResult.failed("Seller profile not found");
            }

            Seller seller = optionalSeller.get();

            // check if he is not approved yet
            if (seller.getStatus() != SellerStatus.APPROVED) {
                return LoginResult.failed("Seller account not yet verified");
            }
        }

        // if it's not seller, just navigate to their respective pages
        return LoginResult.success(user.getRole(), user.getUserId(), user.getFirstName());
    }
}

