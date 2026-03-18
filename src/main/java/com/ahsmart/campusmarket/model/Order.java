package com.ahsmart.campusmarket.model;

import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Users buyer;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "delivery_address_id")
    private Long deliveryAddressId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Delivery is now tracked per order item; this derives an order-level summary for buyer screens.
    public DeliveryStatus getEffectiveDeliveryStatus() {
        if (orderItems == null || orderItems.isEmpty()) {
            return DeliveryStatus.PENDING;
        }

        boolean allDelivered = orderItems.stream()
                .allMatch(item -> item.getDeliveryStatus() == DeliveryStatus.DELIVERED);
        if (allDelivered) {
            return DeliveryStatus.DELIVERED;
        }

        boolean anyBeyondPending = orderItems.stream()
                .anyMatch(item -> item.getDeliveryStatus() == DeliveryStatus.IN_CAMPUS
                        || item.getDeliveryStatus() == DeliveryStatus.DELIVERED);
        return anyBeyondPending ? DeliveryStatus.IN_CAMPUS : DeliveryStatus.PENDING;
    }

    public long getPendingItemCount() {
        return countItemsByStatus(DeliveryStatus.PENDING);
    }

    public long getInCampusItemCount() {
        return countItemsByStatus(DeliveryStatus.IN_CAMPUS);
    }

    public long getDeliveredItemCount() {
        return countItemsByStatus(DeliveryStatus.DELIVERED);
    }

    private long countItemsByStatus(DeliveryStatus status) {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }
        return orderItems.stream()
                .filter(item -> item.getDeliveryStatus() == status)
                .count();
    }
}

