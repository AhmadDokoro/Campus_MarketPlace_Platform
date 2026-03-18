package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.CartItem;
import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.Payment;
import com.ahsmart.campusmarket.model.enums.PaymentMethod;
import com.ahsmart.campusmarket.service.cart.CartService;
import com.ahsmart.campusmarket.service.order.OrderService;
import com.ahsmart.campusmarket.service.payment.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles the buyer purchase flow: checkout page, order placement,
 * order history, and order detail view.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, CartService cartService, PaymentService paymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    // Displays the checkout page with cart items summary and payment method selection.
    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        // Load cart items for the order summary.
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) return "redirect:/cart";

        BigDecimal cartTotal = cartService.getCartTotal(userId);
        int itemCount = cartService.getCartItemCount(userId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("itemCount", itemCount);

        return "order/checkout";
    }

    // Processes the order placement — creates order, then routes based on payment method.
    @PostMapping("/place")
    public String placeOrder(HttpSession session,
                             @RequestParam("paymentMethod") String paymentMethodStr,
                             RedirectAttributes redirectAttributes) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        try {
            // Parse the payment method selected by the buyer.
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr);

            // Create the order from the buyer's cart items.
            Order order = orderService.createOrderFromCart(userId, paymentMethod);

            if (paymentMethod == PaymentMethod.ONLINE_PAYMENT) {
                // Create a pending payment record and redirect to the mock gateway page.
                paymentService.createPendingPayment(order);
                return "redirect:/orders/pay/" + order.getOrderId();
            } else {
                // Campus meetup — order is placed directly, redirect to confirmation.
                return "redirect:/orders/" + order.getOrderId() + "/confirmation";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    // Displays the mock ToyyibPay payment page for online payment orders.
    @GetMapping("/pay/{orderId}")
    public String showPaymentPage(@PathVariable Long orderId, HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        try {
            // Load the order and verify ownership.
            Order order = orderService.getOrderDetail(orderId, userId);

            // Only allow payment for orders that haven't been paid yet.
            if (order.getStatus() != com.ahsmart.campusmarket.model.enums.OrderStatus.PENDING_PAYMENT) {
                return "redirect:/orders/" + orderId + "/confirmation";
            }

            // Ensure a payment record exists (creates one or resets FAILED to PENDING).
            Payment payment = paymentService.createPendingPayment(order);

            model.addAttribute("order", order);
            model.addAttribute("payment", payment);

            return "order/payment";
        } catch (IllegalArgumentException e) {
            return "redirect:/orders/history";
        }
    }

    // Processes the mock payment when the buyer submits card details.
    @PostMapping("/pay/{orderId}")
    public String processPayment(@PathVariable Long orderId,
                                 HttpSession session,
                                 @RequestParam("cardNumber") String cardNumber,
                                 @RequestParam("expiryMonth") String expiryMonth,
                                 @RequestParam("expiryYear") String expiryYear,
                                 @RequestParam("cvv") String cvv,
                                 @RequestParam("cardHolder") String cardHolder,
                                 RedirectAttributes redirectAttributes) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        // Verify user owns this order.
        try {
            orderService.getOrderDetail(orderId, userId);
        } catch (IllegalArgumentException e) {
            return "redirect:/orders/history";
        }

        // Validate card details using the mock gateway.
        var validationResult = paymentService.validateCard(cardNumber, expiryMonth, expiryYear, cvv, cardHolder);
        if (!Boolean.TRUE.equals(validationResult.get("valid"))) {
            redirectAttributes.addFlashAttribute("error", validationResult.get("error"));
            return "redirect:/orders/pay/" + orderId;
        }

        // Process the payment through the mock ToyyibPay gateway.
        String cleanCard = (String) validationResult.get("cleanCard");
        var paymentResult = paymentService.processPayment(orderId, cleanCard);

        if (Boolean.TRUE.equals(paymentResult.get("success"))) {
            // Payment succeeded — redirect to order confirmation page.
            return "redirect:/orders/" + orderId + "/confirmation";
        } else {
            // Payment failed — redirect back to payment page with error.
            redirectAttributes.addFlashAttribute("error", paymentResult.get("message"));
            return "redirect:/orders/pay/" + orderId;
        }
    }

    // Displays the order confirmation page after successful payment/placement.
    @GetMapping("/{orderId}/confirmation")
    public String showConfirmation(@PathVariable Long orderId, HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        try {
            Order order = orderService.getOrderDetail(orderId, userId);
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            model.addAttribute("order", order);
            model.addAttribute("payment", payment);
            return "order/confirmation";
        } catch (IllegalArgumentException e) {
            return "redirect:/orders/history";
        }
    }

    // Displays the buyer's order history page, optionally filtered by tracking status.
    @GetMapping("/history")
    public String showOrderHistory(HttpSession session, Model model,
                                   @RequestParam(value = "filter", required = false) String filter) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        // If a filter is provided, show only matching orders; otherwise show all.
        List<Order> orders;
        if (filter != null && !filter.isBlank()) {
            orders = orderService.getFilteredOrdersForBuyer(userId, filter);
        } else {
            orders = orderService.getOrdersForBuyer(userId);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentFilter", filter);
        return "order/history";
    }

    // Displays a single order's full detail page.
    @GetMapping("/{orderId}")
    public String showOrderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        try {
            Order order = orderService.getOrderDetail(orderId, userId);
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            model.addAttribute("order", order);
            model.addAttribute("payment", payment);
            return "order/detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/orders/history";
        }
    }

    // Safely resolves user ID from session which may be Long or String.
    private Long resolveUserId(HttpSession session) {
        Object obj = session.getAttribute("userId");
        if (obj == null) return null;
        return (obj instanceof Long l) ? l : Long.valueOf(String.valueOf(obj));
    }
}

