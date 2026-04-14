package com.ahsmart.campusmarket.payloadDTOs.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FlaggedProductDTO {
    private final Long productId;
    private final String title;
    private final String description;
    private final BigDecimal price;
    private final Integer quantity;
    private final String conditionName;
    private final String categoryName;
    private final String sellerName;
    private final String primaryImageUrl;
    private final LocalDateTime createdAt;
}
