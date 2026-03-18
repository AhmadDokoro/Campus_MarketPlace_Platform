package com.ahsmart.campusmarket.service.user;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;

    public UserServiceImpl(UsersRepository usersRepository, SellerRepository sellerRepository) {
        this.usersRepository = usersRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public StartSellingDecision decideStartSelling(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Users user = userOpt.get();
        Optional<Seller> sellerOpt = sellerRepository.findByUser(user);

        if (sellerOpt.isEmpty()) {
            // No seller profile yet
            return new StartSellingDecision(null, null);
        }

        Seller seller = sellerOpt.get();
        SellerStatus status = seller.getStatus();

        String reason = null;
        if (status == SellerStatus.REJECTED) {
            reason = seller.getRejectionReason();
        }

        return new StartSellingDecision(status, reason);
    }
}

