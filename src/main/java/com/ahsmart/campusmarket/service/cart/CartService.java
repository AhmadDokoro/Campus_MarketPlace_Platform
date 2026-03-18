package com.ahsmart.campusmarket.service.cart;

import com.ahsmart.campusmarket.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    // Adds a product to the user's cart (creates cart if none exists, increments if product already in cart).
    void addToCart(Long userId, Long productId, int quantity);

    // Returns all items in the user's cart with product details loaded.
    List<CartItem> getCartItems(Long userId);

    // Updates the quantity of a specific cart item.
    void updateItemQuantity(Long userId, Long cartItemId, int newQuantity);

    // Removes a single item from the user's cart.
    void removeItem(Long userId, Long cartItemId);

    // Removes all items from the user's cart (e.g. after checkout).
    void clearCart(Long userId);

    // Returns total number of items in the user's cart (sum of all quantities) for the header badge.
    int getCartItemCount(Long userId);

    // Calculates the total price of all items in the user's cart.
    BigDecimal getCartTotal(Long userId);
}

