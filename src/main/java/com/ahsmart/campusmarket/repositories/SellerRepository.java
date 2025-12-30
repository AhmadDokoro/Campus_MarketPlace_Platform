package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUser(Users user);

    // Find sellers by status (e.g., PENDING)
    List<Seller> findByStatus(SellerStatus status);
}
