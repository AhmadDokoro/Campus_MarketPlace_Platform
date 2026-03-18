package com.ahsmart.campusmarket.payloadDTOs.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuyerOrderTrackingSummaryDTO {
    private long pendingPaymentCount;
    private long placedCount;
    private long inCampusCount;
    private long deliveredCount;
}
