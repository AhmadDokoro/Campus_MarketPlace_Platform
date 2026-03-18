package com.ahsmart.campusmarket.service.order;

import com.ahsmart.campusmarket.model.*;
import com.ahsmart.campusmarket.model.enums.*;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    // Wraps order repositories and cart service for checkout flow.
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    public boolean hasActiveOrdersForProduct(Long productId) {
        // Block deletes when any related order is not final (not delivered or not cancelled/refunded).
        OrderStatus[] finalStatuses = new OrderStatus[]{OrderStatus.CANCELLED, OrderStatus.REFUNDED};
        return orderItemRepository.hasActiveOrdersForProduct(productId, DeliveryStatus.DELIVERED, finalStatuses);
    }

    @Override
    public long countPendingOrdersForSeller(Long sellerId) {
        // Pending orders: payment pending/paid but not delivered yet.
        OrderStatus[] pendingStatuses = new OrderStatus[]{OrderStatus.PENDING_PAYMENT, OrderStatus.PAID};
        return orderRepository.countPendingOrdersForSeller(sellerId, pendingStatuses, DeliveryStatus.DELIVERED);
    }

    @Override
    public long countDeliveredOrdersForSeller(Long sellerId) {
        // Delivered orders: delivery status is DELIVERED and order not cancelled/refunded.
        OrderStatus[] excludedStatuses = new OrderStatus[]{OrderStatus.CANCELLED, OrderStatus.REFUNDED};
        return orderRepository.countDeliveredOrdersForSeller(sellerId, DeliveryStatus.DELIVERED, excludedStatuses);
    }

    // Creates a new order from the buyer's cart: validates stock, builds order items, deducts quantities, clears cart.
    @Override
    @Transactional
    public Order createOrderFromCart(Long userId, PaymentMethod paymentMethod) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in to place an order.");
        }

        // Load the buyer.
        Users buyer = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Load all cart items with product details.
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        // Create the order shell with PENDING_PAYMENT status.
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setDeliveryStatus(DeliveryStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Convert each cart item into an order item.
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Validate that enough stock is available for this item.
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for \"" + product.getTitle()
                        + "\" (available: " + product.getQuantity() + ").");
            }

            // Calculate the subtotal for this line item.
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            // Build the order item.
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setSeller(product.getSeller());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);
            order.getOrderItems().add(orderItem);

            // Accumulate total.
            totalAmount = totalAmount.add(subtotal);

            // Deduct the quantity from product stock.
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        // If payment method is cash on delivery, mark as PAID immediately.
        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            order.setStatus(OrderStatus.PAID);
        }

        // Save the order (cascades to order items).
        Order savedOrder = orderRepository.save(order);

        // Clear the buyer's cart after successful order creation.
        cartService.clearCart(userId);

        return savedOrder;
    }

    // Returns all orders for the buyer, newest first.
    @Override
    public List<Order> getOrdersForBuyer(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in.");
        }
        return orderRepository.findByBuyer_UserIdOrderByCreatedAtDesc(userId);
    }

    // Returns buyer's orders filtered by a tracking category key.
    @Override
    public List<Order> getFilteredOrdersForBuyer(Long userId, String filter) {
        if (userId == null) throw new IllegalArgumentException("You must be logged in.");
        switch (filter) {
            case "pending_payment":
                return orderRepository.findByBuyer_UserIdAndStatusOrderByCreatedAtDesc(userId, OrderStatus.PENDING_PAYMENT);
            case "placed":
                return orderRepository.findByBuyer_UserIdAndDeliveryStatusOrderByCreatedAtDesc(userId, DeliveryStatus.PENDING);
            case "in_campus":
                return orderRepository.findByBuyer_UserIdAndDeliveryStatusOrderByCreatedAtDesc(userId, DeliveryStatus.IN_CAMPUS);
            case "delivered":
                // Show orders delivered in the last 3 days.
                LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
                return orderRepository.findRecentDelivered(userId, DeliveryStatus.DELIVERED, threeDaysAgo);
            default:
                return orderRepository.findByBuyer_UserIdOrderByCreatedAtDesc(userId);
        }
    }

    // Counts buyer's orders with PENDING_PAYMENT status.
    @Override
    public long countPendingPaymentForBuyer(Long userId) {
        return orderRepository.countByBuyer_UserIdAndStatus(userId, OrderStatus.PENDING_PAYMENT);
    }

    // Counts buyer's PAID orders where delivery is still PENDING (order placed, awaiting dispatch).
    @Override
    public long countPlacedForBuyer(Long userId) {
        return orderRepository.countByBuyer_UserIdAndStatusAndDeliveryStatus(userId, OrderStatus.PAID, DeliveryStatus.PENDING);
    }

    // Counts buyer's orders currently in campus delivery.
    @Override
    public long countInCampusForBuyer(Long userId) {
        return orderRepository.countByBuyer_UserIdAndStatusAndDeliveryStatus(userId, OrderStatus.PAID, DeliveryStatus.IN_CAMPUS);
    }

    // Counts buyer's orders delivered within the last 3 days.
    @Override
    public long countRecentDeliveredForBuyer(Long userId) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return orderRepository.countRecentDelivered(userId, DeliveryStatus.DELIVERED, threeDaysAgo);
    }

    // Loads a single order with all details, verifying the caller owns the order.
    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetail(Long orderId, Long userId) {
        // First query: fetch order with items, products, sellers (no images to avoid MultipleBagFetchException).
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        // Security check: make sure the order belongs to this user.
        if (!order.getBuyer().getUserId().equals(userId)) {
            throw new IllegalArgumentException("This order does not belong to you.");
        }

        // Second pass: force-initialize product images within the transaction so templates can access them.
        for (OrderItem item : order.getOrderItems()) {
            item.getProduct().getImages().size();
        }

        return order;
    }
}

