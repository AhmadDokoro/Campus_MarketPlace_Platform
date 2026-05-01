package com.ahsmart.campusmarket.service.order;

import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.UserAddress;
import com.ahsmart.campusmarket.model.enums.PaymentMethod;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderItemChatDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderTrackingSummaryDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerOrderItemDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerSalesHistoryDTO;

import java.util.List;

// Provides order-related checks, counts, and buyer purchase flow operations.
public interface OrderService {

    boolean hasActiveOrdersForProduct(Long productId);

    long countPendingOrdersForSeller(Long sellerId);

    long countDeliveredOrdersForSeller(Long sellerId);

    List<SellerOrderItemDTO> getSellerOrderItems(Long sellerId);

    List<BuyerOrderItemChatDTO> getBuyerOrderItemsForChat(Long buyerUserId);

    BuyerOrderTrackingSummaryDTO getBuyerTrackingSummary(Long buyerUserId);

    void updateDeliveryStatus(Long orderItemId, Long sellerId, com.ahsmart.campusmarket.model.enums.DeliveryStatus newStatus);

    void markOrderItemReceived(Long orderItemId, Long buyerUserId);

    UserAddress getBuyerAddress(Long orderItemId, Long sellerId);

    // Creates a new order from the buyer's cart items and returns the saved order.
    Order createOrderFromCart(Long userId, PaymentMethod paymentMethod);

    // Lists all orders for a buyer, newest first, for the order history page.
    List<Order> getOrdersForBuyer(Long userId);

    // Lists buyer's orders filtered by a status/delivery filter key (e.g. "pending_payment", "placed", "in_campus", "delivered").
    List<Order> getFilteredOrdersForBuyer(Long userId, String filter);

    // Counts buyer's order items whose parent order is still PENDING_PAYMENT.
    long countPendingPaymentForBuyer(Long userId);

    // Counts buyer's paid order items that are still PENDING (order placed, awaiting dispatch).
    long countPlacedForBuyer(Long userId);

    // Counts buyer's paid order items currently in campus delivery.
    long countInCampusForBuyer(Long userId);

    // Counts buyer's paid order items that are currently DELIVERED.
    long countRecentDeliveredForBuyer(Long userId);

    // Loads a single order with full details (items, products, images) for the order detail page.
    Order getOrderDetail(Long orderId, Long userId);

    // Returns all completed (buyer-confirmed) sales for a seller, newest first.
    List<SellerSalesHistoryDTO> getSellerSalesHistory(Long sellerId);

    // Counts completed (buyer-confirmed) sales items for a seller.
    long countCompletedSalesForSeller(Long sellerId);
}

