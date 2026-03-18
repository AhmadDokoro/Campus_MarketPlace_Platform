package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderItemChatDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerOrderItemDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select (count(oi) > 0) from OrderItem oi " +
            "where oi.product.productId = :productId " +
            "and (oi.deliveryStatus <> :deliveredStatus " +
            "or oi.order.status not in :finalStatuses)")
    boolean hasActiveOrdersForProduct(@Param("productId") Long productId,
                                      @Param("deliveredStatus") DeliveryStatus deliveredStatus,
                                      @Param("finalStatuses") OrderStatus[] finalStatuses);

    @Query("select count(oi) from OrderItem oi " +
            "where oi.seller.sellerId = :sellerId " +
            "and oi.order.status = :paidStatus " +
            "and oi.deliveryStatus <> :deliveredStatus")
    long countPendingOrderItemsForSeller(@Param("sellerId") Long sellerId,
                                         @Param("paidStatus") OrderStatus paidStatus,
                                         @Param("deliveredStatus") DeliveryStatus deliveredStatus);

    @Query("select count(oi) from OrderItem oi " +
            "where oi.seller.sellerId = :sellerId " +
            "and oi.order.status = :paidStatus " +
            "and oi.deliveryStatus = :deliveredStatus")
    long countDeliveredOrderItemsForSeller(@Param("sellerId") Long sellerId,
                                           @Param("paidStatus") OrderStatus paidStatus,
                                           @Param("deliveredStatus") DeliveryStatus deliveredStatus);

    @Query("select new com.ahsmart.campusmarket.payloadDTOs.order.SellerOrderItemDTO(" +
            "oi.orderItemId, p.title, oi.quantity, oi.unitPrice, buyer.firstName, buyer.lastName, oi.deliveryStatus, o.orderId) " +
            "from OrderItem oi " +
            "join oi.order o " +
            "join o.buyer buyer " +
            "join oi.product p " +
            "where oi.seller.sellerId = :sellerId " +
            "and o.status = :paidStatus " +
            "order by o.createdAt desc, oi.orderItemId desc")
    List<SellerOrderItemDTO> findSellerOrderItems(@Param("sellerId") Long sellerId,
                                                  @Param("paidStatus") OrderStatus paidStatus);

    @Query("select new com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderItemChatDTO(" +
            "oi.orderItemId, o.orderId, p.title, sellerUser.firstName, sellerUser.lastName, oi.deliveryStatus) " +
            "from OrderItem oi " +
            "join oi.order o " +
            "join oi.product p " +
            "join oi.seller s " +
            "join s.user sellerUser " +
            "where o.buyer.userId = :buyerUserId " +
            "and o.status = :paidStatus " +
            "order by o.createdAt desc, oi.orderItemId desc")
    List<BuyerOrderItemChatDTO> findBuyerOrderItemsForChat(@Param("buyerUserId") Long buyerUserId,
                                                           @Param("paidStatus") OrderStatus paidStatus);

    @Query("select oi from OrderItem oi " +
            "join fetch oi.order o " +
            "join fetch o.buyer " +
            "join fetch oi.seller s " +
            "where oi.orderItemId = :orderItemId")
    Optional<OrderItem> findByIdWithOrderBuyerAndSeller(@Param("orderItemId") Long orderItemId);

    // Finds top-selling product ids by total ordered quantity.
    @Query("select oi.product.productId from OrderItem oi " +
            "where oi.order.status not in :excludedStatuses " +
            "group by oi.product.productId " +
            "order by sum(oi.quantity) desc")
    java.util.List<Long> findTopProductIdsByOrderQuantity(@Param("excludedStatuses") com.ahsmart.campusmarket.model.enums.OrderStatus[] excludedStatuses,
                                                         org.springframework.data.domain.Pageable pageable);
}
