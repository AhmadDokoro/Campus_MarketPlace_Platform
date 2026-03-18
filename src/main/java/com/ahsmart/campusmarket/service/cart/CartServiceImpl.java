package com.ahsmart.campusmarket.service.cart;

import com.ahsmart.campusmarket.model.Cart;
import com.ahsmart.campusmarket.model.CartItem;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.repositories.CartItemRepository;
import com.ahsmart.campusmarket.repositories.CartRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;

    // Adds a product to the user's cart. Creates a new cart if the user doesn't have one yet.
    // If the product is already in the cart, its quantity is incremented instead of duplicating.
    @Override
    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        // Validate that user is logged in.
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in to add items to cart.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        // Fetch the product and check it exists.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        // Find or create the user's cart.
        Cart cart = getOrCreateCart(userId);

        // Check if this product is already in the cart.
        Optional<CartItem> existingItem = cartItemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId);

        if (existingItem.isPresent()) {
            // Product already in cart — increase the quantity.
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Make sure the total doesn't exceed available stock.
            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Cannot add more than available stock (" + product.getQuantity() + ").");
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // New product — validate against stock and create a new cart item.
            if (quantity > product.getQuantity()) {
                throw new IllegalArgumentException("Cannot add more than available stock (" + product.getQuantity() + ").");
            }

            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    // Returns all cart items for the user with product details eagerly loaded.
    @Override
    public List<CartItem> getCartItems(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // Find user's cart, return empty if no cart exists yet.
        Optional<Cart> cart = cartRepository.findByUser_UserId(userId);
        if (cart.isEmpty()) {
            return Collections.emptyList();
        }

        // Load items with product, images, and category eagerly fetched.
        return cartItemRepository.findByCart_CartId(cart.get().getCartId());
    }

    // Updates the quantity of a cart item. Removes the item if new quantity is 0 or less.
    @Override
    @Transactional
    public void updateItemQuantity(Long userId, Long cartItemId, int newQuantity) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in.");
        }

        // Find the cart item and verify it belongs to this user's cart.
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        // Security check: make sure the item belongs to the user's cart.
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("This item does not belong to your cart.");
        }

        // If quantity is 0 or less, remove the item entirely.
        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        // Check that the new quantity doesn't exceed product stock.
        Product product = item.getProduct();
        if (newQuantity > product.getQuantity()) {
            throw new IllegalArgumentException("Cannot exceed available stock (" + product.getQuantity() + ").");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    // Removes a single item from the user's cart.
    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in.");
        }

        // Find the cart item.
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        // Security check: make sure the item belongs to the user's cart.
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("This item does not belong to your cart.");
        }

        cartItemRepository.delete(item);
    }

    // Removes all items from the user's cart (used after checkout).
    @Override
    @Transactional
    public void clearCart(Long userId) {
        if (userId == null) {
            return;
        }

        Optional<Cart> cart = cartRepository.findByUser_UserId(userId);
        // Delete all items in the cart if it exists.
        cart.ifPresent(c -> cartItemRepository.deleteAllByCart_CartId(c.getCartId()));
    }

    // Returns total quantity of all items in the user's cart for the header badge.
    @Override
    public int getCartItemCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return cartItemRepository.countTotalItemsByUserId(userId);
    }

    // Calculates the grand total price: sum of (unit_price * quantity) for each item.
    @Override
    public BigDecimal getCartTotal(Long userId) {
        // Get all items and sum up each item's subtotal.
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Finds the user's existing cart or creates a new one if none exists.
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    // No cart yet — create one for this user.
                    Users user = usersRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found."));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }
}

