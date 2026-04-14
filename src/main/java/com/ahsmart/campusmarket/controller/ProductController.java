package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.UserAddress;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerOrderItemDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.SellerSalesHistoryDTO;
import com.ahsmart.campusmarket.payloadDTOs.review.ProductReviewDTO;
import com.ahsmart.campusmarket.service.category.CategoryService;
import com.ahsmart.campusmarket.service.order.OrderService;
import com.ahsmart.campusmarket.service.product.ProductService;
import com.ahsmart.campusmarket.service.review.ReviewService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seller")
public class ProductController {

    // Handles seller dashboard stats and product CRUD flows.
    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${pexels.api.key}")
    private String pexelsApiKey;

    public ProductController(CategoryService categoryService, ProductService productService, OrderService orderService, ReviewService reviewService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Renders seller dashboard with live product stats.
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            List<Product> products = productService.getProductsForSeller(userId);
            model.addAttribute("products", products);
            model.addAttribute("activeListingsCount", productService.countActiveListings(userId));

            Long sellerId = productService.getSellerIdForUser(userId);
            populateSellerOrdersModel(model, sellerId);
            return "seller/dashboard";
        } catch (IllegalArgumentException ex) {
            return "redirect:/user/start-selling";
        }
    }

    @GetMapping("/orders")
    public String sellerOrders(HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            Long sellerId = productService.getSellerIdForUser(userId);
            populateSellerOrdersModel(model, sellerId);
            return "seller/orders";
        } catch (IllegalArgumentException ex) {
            return "redirect:/user/start-selling";
        }
    }

    @GetMapping("/sales-history")
    public String salesHistory(HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            Long sellerId = productService.getSellerIdForUser(userId);
            List<SellerSalesHistoryDTO> salesHistory = orderService.getSellerSalesHistory(sellerId);
            BigDecimal totalRevenue = salesHistory.stream()
                    .map(SellerSalesHistoryDTO::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("salesHistory", salesHistory);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("completedSalesCount", salesHistory.size());
            return "seller/sales-history";
        } catch (IllegalArgumentException ex) {
            return "redirect:/user/start-selling";
        }
    }

    @GetMapping("/address/{orderItemId}")
    public String buyerAddress(@PathVariable("orderItemId") Long orderItemId,
                               HttpSession session,
                               Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }
        model.addAttribute("orderItemId", orderItemId);

        try {
            Long sellerId = productService.getSellerIdForUser(userId);
            UserAddress buyerAddress = orderService.getBuyerAddress(orderItemId, sellerId);
            model.addAttribute("buyerAddress", buyerAddress);
            return "seller/buyer-address";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "seller/buyer-address";
        }
    }

    @PostMapping("/orders/update-status")
    public String updateSellerOrderStatus(HttpSession session,
                                          @RequestParam("orderItemId") Long orderItemId,
                                          @RequestParam("newStatus") DeliveryStatus newStatus,
                                          RedirectAttributes redirectAttributes) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            Long sellerId = productService.getSellerIdForUser(userId);
            orderService.updateDeliveryStatus(orderItemId, sellerId, newStatus);
            redirectAttributes.addFlashAttribute("success", "Delivery status updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/seller/orders";
    }

    @GetMapping("/products/image-search")
    @ResponseBody
    public ResponseEntity<?> searchImages(@RequestParam("q") String query, HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) return ResponseEntity.status(401).build();
        if (query == null || query.isBlank()) return ResponseEntity.badRequest().body("Query is required.");

        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String apiUrl  = "https://api.pexels.com/v1/search?query=" + encoded + "&per_page=8";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pexelsApiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> raw = restTemplate.exchange(apiUrl, HttpMethod.GET, request, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> photos = (List<Map<String, Object>>) raw.getBody().get("photos");

            List<Map<String, String>> results = new ArrayList<>();
            if (photos != null) {
                for (Map<String, Object> photo : photos) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> src = (Map<String, String>) photo.get("src");
                    Map<String, String> item = new HashMap<>();
                    item.put("id",           String.valueOf(photo.get("id")));
                    item.put("thumbnail",    src.get("medium"));
                    item.put("uploadUrl",    src.get("large"));
                    item.put("photographer", (String) photo.get("photographer"));
                    results.add(item);
                }
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(502).body("Image search unavailable: " + e.getMessage());
        }
    }

    @GetMapping("/products/{productId}/reviews")
    @ResponseBody
    public ResponseEntity<?> getProductReviews(@PathVariable Long productId, HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            productService.getProductForEdit(userId, productId);
            List<ProductReviewDTO> reviews = reviewService.getReviewsByProductId(productId);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        }
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
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                @RequestParam(value = "imageUrl", required = false) String imageUrl,
                                RedirectAttributes redirectAttributes) {
        // Persists a new product and redirects back to the form.
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            productService.createProduct(userId, categoryId, title, description, price, quantity, condition, image, imageUrl);
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
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

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
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

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
        Long userId = resolveUserId(session);
        if (userId == null) {
            return "redirect:/signin";
        }

        try {
            productService.deleteProduct(userId, productId);
            redirectAttributes.addAttribute("success", "Product deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
        }

        return "redirect:/seller/dashboard";
    }

    private Long resolveUserId(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }
        return (userIdObj instanceof Long l) ? l : Long.valueOf(String.valueOf(userIdObj));
    }

    private void populateSellerOrdersModel(Model model, Long sellerId) {
        List<SellerOrderItemDTO> sellerOrderItems = orderService.getSellerOrderItems(sellerId);
        model.addAttribute("sellerOrderItems", sellerOrderItems);
        model.addAttribute("pendingOrdersCount", orderService.countPendingOrdersForSeller(sellerId));
        model.addAttribute("deliveredOrdersCount", orderService.countDeliveredOrdersForSeller(sellerId));
    }
}
