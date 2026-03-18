package com.ahsmart.campusmarket.model;

import com.ahsmart.campusmarket.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    // The order this payment is for (one payment per order).
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Reference ID from the payment provider (mock transaction ID).
    @Column(name = "provider_reference", length = 255)
    private String providerReference;

    // Amount charged for this payment.
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    // Payment lifecycle: PENDING → SUCCESS / FAILED / REFUNDED.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // Timestamp when payment was successfully processed (null if not yet paid).
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

