package com.ahsmart.campusmarket.service.user;

import com.ahsmart.campusmarket.model.enums.SellerStatus;

/**
 * Result of the "Start selling" routing decision.
 */
public record StartSellingDecision(
        SellerStatus sellerStatus,
        String rejectionReason
) {
}

