package com.ahsmart.campusmarket.payloadDTOs.order;

import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SellerOrderItemDTO {

    private final Long orderItemId;
    private final String productTitle;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final String buyerName;
    private final DeliveryStatus deliveryStatus;
    private final Long orderId;

    public SellerOrderItemDTO(Long orderItemId,
                              String productTitle,
                              Integer quantity,
                              BigDecimal unitPrice,
                              String buyerFirstName,
                              String buyerLastName,
                              DeliveryStatus deliveryStatus,
                              Long orderId) {
        this.orderItemId = orderItemId;
        this.productTitle = productTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.buyerName = buildBuyerName(buyerFirstName, buyerLastName);
        this.deliveryStatus = deliveryStatus;
        this.orderId = orderId;
    }

    private String buildBuyerName(String buyerFirstName, String buyerLastName) {
        String first = buyerFirstName == null ? "" : buyerFirstName.trim();
        String last = buyerLastName == null ? "" : buyerLastName.trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? "Unknown Buyer" : fullName;
    }
}
