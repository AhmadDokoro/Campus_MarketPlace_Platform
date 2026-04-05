package com.ahsmart.campusmarket.payloadDTOs.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SellerStatDTO {
    private final Long sellerId;
    private final String sellerName;
    private final LocalDateTime submittedAt;
    private final long listingCount;
    private final String statusName;
}
