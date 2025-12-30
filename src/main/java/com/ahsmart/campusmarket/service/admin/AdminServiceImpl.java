package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    private final SellerRepository sellerRepository; // ...existing code...
    private final UsersRepository usersRepository; // ...existing code...

    public AdminServiceImpl(SellerRepository sellerRepository, UsersRepository usersRepository) {
        this.sellerRepository = sellerRepository; // assign seller repo
        this.usersRepository = usersRepository; // assign users repo
    }

    @Override
    public List<Seller> getPendingSellers() {
        // return all sellers where status == PENDING
        return sellerRepository.findByStatus(SellerStatus.PENDING);
    }

    @Override
    public Seller getSellerForReview(Long sellerId) {
        // fetch seller by id or throw IllegalArgumentException
        Optional<Seller> optional = sellerRepository.findById(sellerId);
        return optional.orElseThrow(() -> new IllegalArgumentException("Seller not found"));
    }

    @Override
    @Transactional
    public Seller reviewSeller(Long sellerId, SellerStatus status, Long reviewerId) {
        // fetch seller
        Seller seller = getSellerForReview(sellerId); // reuse method
        // set status
        seller.setStatus(status);
        // if reviewerId provided, load reviewer user and set
        if (reviewerId != null) {
            Optional<Users> reviewerOpt = usersRepository.findById(reviewerId);
            reviewerOpt.ifPresent(seller::setReviewer);
        }
        // persist
        return sellerRepository.save(seller);
    }
}

