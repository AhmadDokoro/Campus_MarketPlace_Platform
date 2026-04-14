package com.ahsmart.campusmarket.payloadDTOs.order;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class SellerSalesHistoryDTO {

    private final Long orderItemId;
    private final String productTitle;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final String buyerName;
    private final Long orderId;
    private final LocalDateTime orderedAt;
    private final Long productId;

    public SellerSalesHistoryDTO(Long orderItemId,
                                 String productTitle,
                                 Integer quantity,
                                 BigDecimal unitPrice,
                                 BigDecimal subtotal,
                                 String buyerFirstName,
                                 String buyerLastName,
                                 Long orderId,
                                 LocalDateTime orderedAt,
                                 Long productId) {
        this.orderItemId = orderItemId;
        this.productTitle = productTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.buyerName = buildBuyerName(buyerFirstName, buyerLastName);
        this.orderId = orderId;
        this.orderedAt = orderedAt;
        this.productId = productId;
    }

    private String buildBuyerName(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Unknown Buyer" : full;
    }
}
