package com.ahsmart.campusmarket.service.report;

import com.ahsmart.campusmarket.payloadDTOs.admin.CategoryStatsDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.FlaggedProductDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.SellerStatDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.WeeklyListingDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AdminReportData(
        AdminReportPeriod period,
        LocalDateTime generatedAt,
        long totalUsers,
        long totalSellers,
        long verifiedSellers,
        long activeListings,
        long flaggedListings,
        long totalSales,
        Map<String, Long> verificationStats,
        List<CategoryStatsDTO> topCategories,
        List<WeeklyListingDTO> listingActivity,
        String listingActivityLabel,
        List<SellerStatDTO> sellerStats,
        List<FlaggedProductDTO> flaggedProducts
) {

    public boolean isEmpty() {
        return totalUsers == 0
                && totalSellers == 0
                && verifiedSellers == 0
                && activeListings == 0
                && flaggedListings == 0
                && totalSales == 0
                && topCategories.isEmpty()
                && sellerStats.isEmpty()
                && flaggedProducts.isEmpty();
    }
}
