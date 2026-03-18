package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select distinct o from Order o " +
            "left join fetch o.orderItems oi " +
            "where o.buyer.userId = :userId " +
            "order by o.createdAt desc")
    List<Order> findBuyerOrdersWithItems(@Param("userId") Long userId);

    // Counts buyer's orders that still need payment (status = PENDING_PAYMENT).
    long countByBuyer_UserIdAndStatus(Long userId, OrderStatus status);

    // Loads a single order with its items, products, and sellers (NOT images — fetched separately to avoid MultipleBagFetchException).
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH oi.seller " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}

