package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select (count(oi) > 0) from OrderItem oi " +
            "where oi.product.productId = :productId " +
            "and (oi.order.deliveryStatus <> :deliveredStatus " +
            "or oi.order.status not in :finalStatuses)")
    boolean hasActiveOrdersForProduct(@Param("productId") Long productId,
                                      @Param("deliveredStatus") DeliveryStatus deliveredStatus,
                                      @Param("finalStatuses") OrderStatus[] finalStatuses);

    // Finds top-selling product ids by total ordered quantity.
    @Query("select oi.product.productId from OrderItem oi " +
            "where oi.order.status not in :excludedStatuses " +
            "group by oi.product.productId " +
            "order by sum(oi.quantity) desc")
    java.util.List<Long> findTopProductIdsByOrderQuantity(@Param("excludedStatuses") com.ahsmart.campusmarket.model.enums.OrderStatus[] excludedStatuses,
                                                         org.springframework.data.domain.Pageable pageable);
}
