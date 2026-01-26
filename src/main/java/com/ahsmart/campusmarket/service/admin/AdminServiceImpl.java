package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.helper.EmailHelper;
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

    private final SellerRepository sellerRepository;
    private final UsersRepository usersRepository;
    private final EmailHelper emailHelper;

    public AdminServiceImpl(SellerRepository sellerRepository, UsersRepository usersRepository, EmailHelper emailHelper) {
        this.sellerRepository = sellerRepository; // assign seller repo
        this.usersRepository = usersRepository; // assign users repo
        this.emailHelper = emailHelper;
    }

    @Override
    public List<Seller> getPendingSellers() {
        // return all sellers where status == PENDING
        return sellerRepository.findByStatus(SellerStatus.PENDING);
    }

    @Override
    public Seller getSellerForReview(Long sellerId) {
        // fetch seller by id with user + mentor loaded
        Optional<Seller> optional = sellerRepository.findByIdWithUserAndMentor(sellerId);
        return optional.orElseThrow(() -> new IllegalArgumentException("Seller not found"));
    }

    @Override
    @Transactional
    public Seller reviewSeller(Long sellerId, SellerStatus status, Long reviewerId) {
        Seller seller = getSellerForReview(sellerId);
        seller.setStatus(status);

        if (reviewerId != null) {
            Optional<Users> reviewerOpt = usersRepository.findById(reviewerId);
            reviewerOpt.ifPresent(seller::setReviewer);
        }

        // Keep rejectionReason ONLY for rejected sellers.
        if (status == SellerStatus.APPROVED) {
            seller.setRejectionReason(null);
            emailHelper.sendEmail(
                seller.getUser().getEmail(),
                "Seller Verification Approved – Campus Marketplace Platform",
                "Dear " + seller.getUser().getFirstName() + ",\n\n" +
                "Congratulations! Your seller verification request has been approved.\n" +
                "You can now list products and start selling on the Campus Marketplace Platform.\n\n" +
                "Best regards,\nCampus Marketplace Platform"
            );
        }

        return sellerRepository.save(seller);
    }

    @Override
    @Transactional
    public Seller rejectSeller(Long sellerId, Long reviewerId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        String trimmed = rejectionReason.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("Rejection reason must be 500 characters or less");
        }

        Seller seller = getSellerForReview(sellerId);
        seller.setStatus(SellerStatus.REJECTED);
        seller.setRejectionReason(trimmed);

        if (reviewerId != null) {
            usersRepository.findById(reviewerId).ifPresent(seller::setReviewer);
        }

        emailHelper.sendEmail(
            seller.getUser().getEmail(),
            "Seller Verification Rejected – Campus Marketplace Platform",
            "Dear " + seller.getUser().getFirstName() + ",\n\n" +
            "Your seller verification request has been rejected for the following reason:\n" +
            rejectionReason + "\n\n" +
            "You may correct the issue and reapply.\n\n" +
            "Best regards,\nCampus Marketplace Platform"
        );

        return sellerRepository.save(seller);
    }
}
