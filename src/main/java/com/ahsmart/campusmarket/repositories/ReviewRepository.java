package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Checks if a review already exists for this order by this buyer (prevents duplicates).
    boolean existsByOrder_OrderIdAndReviewer_UserId(Long orderId, Long reviewerId);

    // Returns order IDs already reviewed by this buyer for the profile page UI.
    @Query("SELECT r.order.orderId FROM Review r WHERE r.reviewer.userId = :reviewerId")
    Set<Long> findReviewedOrderIdsByReviewer(@Param("reviewerId") Long reviewerId);
}
