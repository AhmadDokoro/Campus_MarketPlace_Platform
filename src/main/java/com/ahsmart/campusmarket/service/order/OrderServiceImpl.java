package com.ahsmart.campusmarket.service.order;

import com.ahsmart.campusmarket.model.*;
import com.ahsmart.campusmarket.model.enums.*;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.UserAddressRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderItemChatDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderTrackingSummaryDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerOrderItemDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerSalesHistoryDTO;
import com.ahsmart.campusmarket.service.cart.CartService;
import com.ahsmart.campusmarket.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final UserAddressRepository userAddressRepository;
    private final ChatService chatService;

    @Override
    public boolean hasActiveOrdersForProduct(Long productId) {
        // Block deletes when any related order is not final (not delivered or not cancelled/refunded).
        OrderStatus[] finalStatuses = new OrderStatus[]{OrderStatus.CANCELLED, OrderStatus.REFUNDED};
        return orderItemRepository.hasActiveOrdersForProduct(productId, DeliveryStatus.DELIVERED, finalStatuses);
    }

    @Override
    public long countPendingOrdersForSeller(Long sellerId) {
        // Seller dashboard is per-product: only paid items not yet delivered are pending.
        return orderItemRepository.countPendingOrderItemsForSeller(sellerId, OrderStatus.PAID, DeliveryStatus.DELIVERED);
    }

    @Override
    public long countDeliveredOrdersForSeller(Long sellerId) {
        // Delivered seller items are counted independently per order_item.
        return orderItemRepository.countDeliveredOrderItemsForSeller(sellerId, OrderStatus.PAID, DeliveryStatus.DELIVERED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderItemDTO> getSellerOrderItems(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller id is required.");
        }
        return orderItemRepository.findSellerOrderItems(sellerId, OrderStatus.PAID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuyerOrderItemChatDTO> getBuyerOrderItemsForChat(Long buyerUserId) {
        if (buyerUserId == null) {
            throw new IllegalArgumentException("Buyer user id is required.");
        }
        return orderItemRepository.findBuyerOrderItemsForChat(buyerUserId, OrderStatus.PAID);
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerOrderTrackingSummaryDTO getBuyerTrackingSummary(Long buyerUserId) {
        List<Order> orders = loadBuyerOrdersWithItems(buyerUserId);

        long pendingPaymentCount = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING_PAYMENT)
                .mapToLong(order -> order.getOrderItems() == null ? 0L : order.getOrderItems().size())
                .sum();
        long placedCount = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getDeliveryStatus() == DeliveryStatus.PENDING)
                .count();
        long inCampusCount = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getDeliveryStatus() == DeliveryStatus.IN_CAMPUS)
                .count();
        long deliveredCount = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getDeliveryStatus() == DeliveryStatus.DELIVERED)
                .count();

        return new BuyerOrderTrackingSummaryDTO(pendingPaymentCount, placedCount, inCampusCount, deliveredCount);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long orderItemId, Long sellerId, DeliveryStatus newStatus) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("Order item id is required.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller id is required.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New delivery status is required.");
        }
        if (newStatus != DeliveryStatus.IN_CAMPUS && newStatus != DeliveryStatus.DELIVERED) {
            throw new IllegalArgumentException("Only IN_CAMPUS or DELIVERED updates are allowed.");
        }

        OrderItem orderItem = getSellerOwnedOrderItem(orderItemId, sellerId);
        if (orderItem.getOrder().getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Only paid order items can be updated.");
        }

        DeliveryStatus currentStatus = orderItem.getDeliveryStatus();
        if (currentStatus == DeliveryStatus.DELIVERED || currentStatus == DeliveryStatus.RECEIVED) {
            throw new IllegalArgumentException("Delivered order items cannot be updated again.");
        }
        if (currentStatus == DeliveryStatus.PENDING && newStatus != DeliveryStatus.IN_CAMPUS) {
            throw new IllegalArgumentException("Pending items must be marked IN_CAMPUS first.");
        }
        if (currentStatus == DeliveryStatus.IN_CAMPUS && newStatus != DeliveryStatus.DELIVERED) {
            throw new IllegalArgumentException("In-campus items can only be marked DELIVERED.");
        }

        orderItem.setDeliveryStatus(newStatus);
        orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional
    public void markOrderItemReceived(Long orderItemId, Long buyerUserId) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("Order item id is required.");
        }
        if (buyerUserId == null) {
            throw new IllegalArgumentException("Buyer user id is required.");
        }

        OrderItem orderItem = orderItemRepository.findByIdWithOrderBuyerAndSeller(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));

        if (!orderItem.getOrder().getBuyer().getUserId().equals(buyerUserId)) {
            throw new IllegalArgumentException("You cannot access another buyer's order item.");
        }
        if (orderItem.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw new IllegalArgumentException("Only delivered order items can be marked as received.");
        }

        orderItem.setDeliveryStatus(DeliveryStatus.RECEIVED);
        orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddress getBuyerAddress(Long orderItemId, Long sellerId) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("Order item id is required.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller id is required.");
        }

        OrderItem orderItem = getSellerOwnedOrderItem(orderItemId, sellerId);
        Long buyerUserId = orderItem.getOrder().getBuyer().getUserId();

        if (orderItem.getOrder().getDeliveryAddressId() != null) {
            return userAddressRepository.findByAddressIdAndUser_UserId(orderItem.getOrder().getDeliveryAddressId(), buyerUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Buyer address not found."));
        }

        return userAddressRepository.findFirstByUser_UserIdOrderByAddressIdDesc(buyerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer address not found."));
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

        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            savedOrder.getOrderItems().forEach(chatService::createChatForOrderItem);
        }

        // Clear the buyer's cart after successful order creation.
        cartService.clearCart(userId);

        return savedOrder;
    }

    // Returns all orders for the buyer, newest first.
    @Override
    public List<Order> getOrdersForBuyer(Long userId) {
        return loadBuyerOrdersWithItems(userId);
    }

    // Returns buyer's orders filtered by a tracking category key.
    @Override
    public List<Order> getFilteredOrdersForBuyer(Long userId, String filter) {
        List<Order> orders = loadBuyerOrdersWithItems(userId);
        switch (filter) {
            case "pending_payment":
                return orders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.PENDING_PAYMENT)
                        .toList();
            case "placed":
                return orders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.PAID)
                        .filter(order -> order.getEffectiveDeliveryStatus() == DeliveryStatus.PENDING)
                        .toList();
            case "in_campus":
                return orders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.PAID)
                        .filter(order -> order.getEffectiveDeliveryStatus() == DeliveryStatus.IN_CAMPUS)
                        .toList();
            case "delivered":
                return orders.stream()
                        .filter(order -> order.getStatus() == OrderStatus.PAID)
                        .filter(order -> order.getEffectiveDeliveryStatus() == DeliveryStatus.DELIVERED)
                        .toList();
            default:
                return orders;
        }
    }

    // Counts buyer's orders with PENDING_PAYMENT status.
    @Override
    public long countPendingPaymentForBuyer(Long userId) {
        return getBuyerTrackingSummary(userId).getPendingPaymentCount();
    }

    // Counts buyer's PAID orders where delivery is still PENDING (order placed, awaiting dispatch).
    @Override
    public long countPlacedForBuyer(Long userId) {
        return getBuyerTrackingSummary(userId).getPlacedCount();
    }

    // Counts buyer's orders currently in campus delivery.
    @Override
    public long countInCampusForBuyer(Long userId) {
        return getBuyerTrackingSummary(userId).getInCampusCount();
    }

    // Counts buyer's orders delivered within the last 3 days.
    @Override
    public long countRecentDeliveredForBuyer(Long userId) {
        return getBuyerTrackingSummary(userId).getDeliveredCount();
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

    private List<Order> loadBuyerOrdersWithItems(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in.");
        }
        return orderRepository.findBuyerOrdersWithItems(userId);
    }

    private OrderItem getSellerOwnedOrderItem(Long orderItemId, Long sellerId) {
        OrderItem orderItem = orderItemRepository.findByIdWithOrderBuyerAndSeller(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));

        if (!orderItem.getSeller().getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("You cannot access another seller's order item.");
        }

        return orderItem;
    }

    // Returns all completed (buyer-confirmed RECEIVED) sales for a seller, newest first.
    @Override
    @Transactional(readOnly = true)
    public List<SellerSalesHistoryDTO> getSellerSalesHistory(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller id is required.");
        }
        return orderItemRepository.findSellerSalesHistory(sellerId, OrderStatus.PAID, DeliveryStatus.RECEIVED);
    }

    // Counts completed (buyer-confirmed RECEIVED) sales items for a seller.
    @Override
    public long countCompletedSalesForSeller(Long sellerId) {
        if (sellerId == null) {
            return 0L;
        }
        return orderItemRepository.countCompletedSalesForSeller(sellerId, OrderStatus.PAID, DeliveryStatus.RECEIVED);
    }
}

