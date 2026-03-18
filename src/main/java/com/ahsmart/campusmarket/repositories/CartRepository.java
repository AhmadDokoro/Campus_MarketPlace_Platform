package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Finds the single cart that belongs to a user (one cart per user).
    Optional<Cart> findByUser_UserId(Long userId);
}

