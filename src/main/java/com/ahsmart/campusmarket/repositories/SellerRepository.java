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

    // Count sellers by verification status (admin analytics).
    long countByStatus(SellerStatus status);

    // Returns seller name, join date, status and product count for admin manage-sellers table.
    @Query(value = "SELECT s.seller_id, u.first_name, u.last_name, s.submitted_at, s.status, COUNT(p.product_id) AS listing_count " +
            "FROM sellers s " +
            "JOIN users u ON s.user_id = u.user_id " +
            "LEFT JOIN products p ON p.seller_id = s.seller_id " +
            "GROUP BY s.seller_id, u.first_name, u.last_name, s.submitted_at, s.status " +
            "ORDER BY listing_count DESC", nativeQuery = true)
    List<Object[]> findSellerStats();
}
