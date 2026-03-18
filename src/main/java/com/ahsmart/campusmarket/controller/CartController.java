package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.CartItem;
import com.ahsmart.campusmarket.service.cart.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    // Handles all shopping cart operations for buyers.
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Displays the cart page with all items, subtotals, and grand total.
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        // Redirect to login if user is not authenticated.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }
        Long userId = resolveUserId(userIdObj);

        // Fetch all cart items with product details for display.
        List<CartItem> items = cartService.getCartItems(userId);
        BigDecimal cartTotal = cartService.getCartTotal(userId);
        int itemCount = cartService.getCartItemCount(userId);

        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("itemCount", itemCount);

        return "cart/cart";
    }

    // Adds a product to the user's cart via AJAX and returns JSON response.
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(HttpSession session,
                                         @RequestParam("productId") Long productId,
                                         @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        Map<String, Object> response = new HashMap<>();

        // Check if user is logged in.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            response.put("success", false);
            response.put("message", "Please log in to add items to cart.");
            response.put("redirect", "/signin");
            return response;
        }
        Long userId = resolveUserId(userIdObj);

        try {
            // Delegate to service to add product to cart.
            cartService.addToCart(userId, productId, quantity);
            // Return updated cart count for the header badge.
            int newCount = cartService.getCartItemCount(userId);
            response.put("success", true);
            response.put("message", "Added to cart!");
            response.put("cartCount", newCount);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Updates the quantity of a cart item via AJAX and returns updated totals.
    @PostMapping("/update/{cartItemId}")
    @ResponseBody
    public Map<String, Object> updateQuantity(HttpSession session,
                                               @PathVariable Long cartItemId,
                                               @RequestParam("quantity") int quantity) {
        Map<String, Object> response = new HashMap<>();

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            response.put("success", false);
            response.put("message", "Please log in.");
            return response;
        }
        Long userId = resolveUserId(userIdObj);

        try {
            // Update the item quantity (removes item if quantity is 0).
            cartService.updateItemQuantity(userId, cartItemId, quantity);
            // Return updated cart count and total for live UI refresh.
            response.put("success", true);
            response.put("cartCount", cartService.getCartItemCount(userId));
            response.put("cartTotal", cartService.getCartTotal(userId));
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Removes a single item from the cart via AJAX.
    @PostMapping("/remove/{cartItemId}")
    @ResponseBody
    public Map<String, Object> removeItem(HttpSession session,
                                           @PathVariable Long cartItemId) {
        Map<String, Object> response = new HashMap<>();

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            response.put("success", false);
            response.put("message", "Please log in.");
            return response;
        }
        Long userId = resolveUserId(userIdObj);

        try {
            // Remove the item from the cart.
            cartService.removeItem(userId, cartItemId);
            response.put("success", true);
            response.put("cartCount", cartService.getCartItemCount(userId));
            response.put("cartTotal", cartService.getCartTotal(userId));
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Returns current cart item count as JSON for live header badge updates.
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            response.put("count", 0);
            return response;
        }
        Long userId = resolveUserId(userIdObj);
        response.put("count", cartService.getCartItemCount(userId));
        return response;
    }

    // Safely resolves user ID from session which may be Long or String.
    private Long resolveUserId(Object userIdObj) {
        return (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));
    }
}

