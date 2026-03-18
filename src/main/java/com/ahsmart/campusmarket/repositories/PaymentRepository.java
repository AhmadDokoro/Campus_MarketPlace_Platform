package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Finds the payment record linked to a specific order.
    Optional<Payment> findByOrder_OrderId(Long orderId);

    // Finds a payment by its provider reference (mock transaction ID).
    Optional<Payment> findByProviderReference(String providerReference);
}

