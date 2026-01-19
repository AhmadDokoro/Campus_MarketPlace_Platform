package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUser(Users user);

    // Find sellers by status (e.g., PENDING)
    List<Seller> findByStatus(SellerStatus status);

    // Fetch seller + user + mentor for admin review page to avoid LazyInitializationException
    @Query("select s from Seller s join fetch s.user u left join fetch u.mentor where s.sellerId = :sellerId")
    Optional<Seller> findByIdWithUserAndMentor(@Param("sellerId") Long sellerId);
}
