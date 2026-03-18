package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select count(distinct oi.order.orderId) from OrderItem oi " +
            "where oi.seller.sellerId = :sellerId " +
            "and oi.order.status in :statuses " +
            "and oi.order.deliveryStatus <> :deliveredStatus")
    long countPendingOrdersForSeller(@Param("sellerId") Long sellerId,
                                     @Param("statuses") OrderStatus[] statuses,
                                     @Param("deliveredStatus") DeliveryStatus deliveredStatus);

    // we get all the orders count that are not canceled or refunded to decide whether the product should be deleted.
    @Query("select count(distinct oi.order.orderId) from OrderItem oi " +
            "where oi.seller.sellerId = :sellerId " +
            "and oi.order.deliveryStatus = :deliveredStatus " +
            "and oi.order.status not in :excludedStatuses")
    long countDeliveredOrdersForSeller(@Param("sellerId") Long sellerId,
                                       @Param("deliveredStatus") DeliveryStatus deliveredStatus,
                                       @Param("excludedStatuses") OrderStatus[] excludedStatuses);

    // Loads all orders for a buyer sorted by newest first, for the order history page.
    List<Order> findByBuyer_UserIdOrderByCreatedAtDesc(Long userId);

    // Counts buyer's orders that still need payment (status = PENDING_PAYMENT).
    long countByBuyer_UserIdAndStatus(Long userId, OrderStatus status);

    // Counts buyer's orders by payment status and delivery status combo (e.g. PAID + PENDING).
    long countByBuyer_UserIdAndStatusAndDeliveryStatus(Long userId, OrderStatus status, DeliveryStatus deliveryStatus);

    // Counts buyer's recently delivered orders (DELIVERED within the last N days).
    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.userId = :userId " +
            "AND o.deliveryStatus = :deliveryStatus " +
            "AND o.updatedAt >= :since")
    long countRecentDelivered(@Param("userId") Long userId,
                              @Param("deliveryStatus") DeliveryStatus deliveryStatus,
                              @Param("since") java.time.LocalDateTime since);

    // Loads buyer's orders filtered by payment status (for history page filtering).
    List<Order> findByBuyer_UserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    // Loads buyer's orders filtered by delivery status (for history page filtering).
    List<Order> findByBuyer_UserIdAndDeliveryStatusOrderByCreatedAtDesc(Long userId, DeliveryStatus deliveryStatus);

    // Loads buyer's recently delivered orders (for history page filtering).
    @Query("SELECT o FROM Order o WHERE o.buyer.userId = :userId " +
            "AND o.deliveryStatus = :deliveryStatus " +
            "AND o.updatedAt >= :since ORDER BY o.updatedAt DESC")
    List<Order> findRecentDelivered(@Param("userId") Long userId,
                                    @Param("deliveryStatus") DeliveryStatus deliveryStatus,
                                    @Param("since") java.time.LocalDateTime since);

    // Loads a single order with its items, products, and sellers (NOT images — fetched separately to avoid MultipleBagFetchException).
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH oi.seller " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}

