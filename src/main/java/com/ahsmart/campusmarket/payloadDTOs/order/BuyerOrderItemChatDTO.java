package com.ahsmart.campusmarket.payloadDTOs.order;

import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import lombok.Getter;

@Getter
public class BuyerOrderItemChatDTO {

    private final Long orderItemId;
    private final Long orderId;
    private final String productTitle;
    private final String sellerName;
    private final DeliveryStatus deliveryStatus;

    public BuyerOrderItemChatDTO(Long orderItemId,
                                 Long orderId,
                                 String productTitle,
                                 String sellerFirstName,
                                 String sellerLastName,
                                 DeliveryStatus deliveryStatus) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productTitle = productTitle;
        this.sellerName = buildSellerName(sellerFirstName, sellerLastName);
        this.deliveryStatus = deliveryStatus;
    }

    private String buildSellerName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? "Seller" : fullName;
    }
}
