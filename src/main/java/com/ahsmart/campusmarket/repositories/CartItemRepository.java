package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Checks if a specific product is already in the cart (to increment instead of duplicate).
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);

    // Loads all items in a cart with product, images, category, and seller eagerly fetched.
    @EntityGraph(attributePaths = {"product", "product.images", "product.category", "product.seller"})
    List<CartItem> findByCart_CartId(Long cartId);

    // Counts total number of items in a user's cart for the header badge.
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.user.userId = :userId")
    int countTotalItemsByUserId(@Param("userId") Long userId);

    // Deletes all items in a cart (used for clear cart after checkout).
    void deleteAllByCart_CartId(Long cartId);
}

