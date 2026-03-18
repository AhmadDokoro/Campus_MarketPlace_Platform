package com.ahsmart.campusmarket.service.payment;

import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.Payment;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.model.enums.PaymentStatus;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.PaymentRepository;
import com.ahsmart.campusmarket.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Manages payment lifecycle: creating pending records, processing via mock gateway,
 * and updating both payment and order statuses.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MockToyyibPayService mockToyyibPay;
    private final ChatService chatService;

    // Creates or resets a PENDING payment record for the given order before redirecting to the gateway.
    @Transactional
    public Payment createPendingPayment(Order order) {
        // Check if payment already exists for this order.
        var existing = paymentRepository.findByOrder_OrderId(order.getOrderId());
        if (existing.isPresent()) {
            Payment payment = existing.get();
            // If previous payment failed, reset to PENDING so buyer can retry.
            if (payment.getStatus() == PaymentStatus.FAILED) {
                payment.setStatus(PaymentStatus.PENDING);
                payment.setProviderReference(null);
                return paymentRepository.save(payment);
            }
            // Already pending or succeeded — just return it.
            return payment;
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    // Validates card details using the mock gateway.
    public Map<String, Object> validateCard(String cardNumber, String expiryMonth,
                                             String expiryYear, String cvv, String cardHolder) {
        return mockToyyibPay.validateCard(cardNumber, expiryMonth, expiryYear, cvv, cardHolder);
    }

    // Processes the payment through the mock ToyyibPay gateway and updates statuses accordingly.
    @Transactional
    public Map<String, Object> processPayment(Long orderId, String cleanCardNumber) {
        // Find the pending payment for this order.
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No payment found for order #" + orderId));

        // Only allow processing if payment is PENDING or FAILED (retry).
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.FAILED) {
            throw new IllegalArgumentException("Payment has already been processed.");
        }

        // Call the mock gateway to process the payment.
        Map<String, Object> gatewayResult = mockToyyibPay.processPayment(cleanCardNumber, payment.getAmount());

        // Store the transaction reference from the gateway.
        payment.setProviderReference((String) gatewayResult.get("reference"));

        if (Boolean.TRUE.equals(gatewayResult.get("success"))) {
            // Payment succeeded — update payment and order status.
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Update the order status from PENDING_PAYMENT to PAID.
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // Each paid order item gets exactly one chat anchored on order_item_id.
            order.getOrderItems().forEach(chatService::createChatForOrderItem);
        } else {
            // Payment failed — mark payment as failed, keep order as PENDING_PAYMENT.
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        return gatewayResult;
    }

    // Finds the payment record for a specific order.
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrder_OrderId(orderId).orElse(null);
    }
}



