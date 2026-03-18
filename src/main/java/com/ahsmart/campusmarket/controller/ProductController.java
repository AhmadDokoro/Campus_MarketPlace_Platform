package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.service.category.CategoryService;
import com.ahsmart.campusmarket.service.order.OrderService;
import com.ahsmart.campusmarket.service.product.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/seller")
public class ProductController {

    // Handles seller dashboard stats and product CRUD flows.
    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;

    public ProductController(CategoryService categoryService, ProductService productService, OrderService orderService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Renders seller dashboard with live product stats.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }
        Long userId = (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));

        List<Product> products = productService.getProductsForSeller(userId);
        model.addAttribute("products", products);
        model.addAttribute("activeListingsCount", productService.countActiveListings(userId));

        Long sellerId = productService.getSellerIdForUser(userId);
        model.addAttribute("pendingOrdersCount", orderService.countPendingOrdersForSeller(sellerId));
        model.addAttribute("deliveredOrdersCount", orderService.countDeliveredOrdersForSeller(sellerId));

        return "seller/dashboard";
    }

    @GetMapping("/products/new")
    public String addProductPage(HttpSession session, Model model,
                                 @RequestParam(value = "success", required = false) String success,
                                 @RequestParam(value = "error", required = false) String error) {
        // Shows the add-product form.
        if (session.getAttribute("userId") == null) {
            return "redirect:/signin";
        }
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        return "product-listings/add-product";
    }

    @PostMapping("/products")
    public String createProduct(HttpSession session,
                                @RequestParam("categoryId") Long categoryId,
                                @RequestParam("title") String title,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam("price") BigDecimal price,
                                @RequestParam("quantity") Integer quantity,
                                @RequestParam("condition") Condition condition,
                                @RequestParam("image") MultipartFile image,
                                RedirectAttributes redirectAttributes) {
        // Persists a new product and redirects back to the form.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }

        Long userId = (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));

        try {
            productService.createProduct(userId, categoryId, title, description, price, quantity, condition, image);
            redirectAttributes.addAttribute("success", "Product added successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }

        return "redirect:/seller/products/new";
    }

    @GetMapping("/products/{productId}/edit")
    public String editProductPage(HttpSession session, Model model,
                                  @RequestParam(value = "error", required = false) String error,
                                  @RequestParam(value = "success", required = false) String success,
                                  @PathVariable("productId") Long productId) {
        // Shows the edit-product form for the seller.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }
        Long userId = (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));

        Product product = productService.getProductForEdit(userId, productId);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        return "product-listings/edit-product";
    }



    @PostMapping("/products/{productId}/edit")
    public String updateProduct(HttpSession session,
                                @PathVariable("productId") Long productId,
                                @RequestParam("categoryId") Long categoryId,
                                @RequestParam("title") String title,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam("price") BigDecimal price,
                                @RequestParam("quantity") Integer quantity,
                                @RequestParam("condition") Condition condition,
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                RedirectAttributes redirectAttributes) {
        // Updates a product and redirects back to edit form.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }
        Long userId = (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));

        try {
            productService.updateProduct(userId, productId, categoryId, title, description, price, quantity, condition, image);
            redirectAttributes.addAttribute("success", "Product updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }

        return "redirect:/seller/products/" + productId + "/edit";
    }




    @PostMapping("/products/{productId}/delete")
    public String deleteProduct(HttpSession session,
                                @PathVariable("productId") Long productId,
                                RedirectAttributes redirectAttributes) {
        // Deletes a product if it has no active orders.
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }
        Long userId = (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));

        try {
            productService.deleteProduct(userId, productId);
            redirectAttributes.addAttribute("success", "Product deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }

        return "redirect:/seller/dashboard";
    }
}
