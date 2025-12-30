package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.enums.SellerStatus;

import java.util.List;

public interface AdminService {

    // Get all sellers with PENDING status for admin review
    List<Seller> getPendingSellers();

    // Get single seller by id for review
    Seller getSellerForReview(Long sellerId);

    // Review a seller: approve or reject and set reviewer id
    Seller reviewSeller(Long sellerId, SellerStatus status, Long reviewerId);
}

