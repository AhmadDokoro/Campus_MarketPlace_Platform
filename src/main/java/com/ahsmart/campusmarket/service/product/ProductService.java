package com.ahsmart.campusmarket.service.product;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.enums.Condition;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // Creates a product with a single primary image.
    Product createProduct(Long userId,
                          Long categoryId,
                          String title,
                          String description,
                          BigDecimal price,
                          Integer quantity,
                          Condition condition,
                          MultipartFile imageFile);

    // Lists all products for the seller to render in the dashboard table.
    List<Product> getProductsForSeller(Long userId);

    // Returns a product owned by the seller for editing.
    Product getProductForEdit(Long userId, Long productId);

    // Updates a product and optionally replaces its primary image.
    Product updateProduct(Long userId,
                          Long productId,
                          Long categoryId,
                          String title,
                          String description,
                          BigDecimal price,
                          Integer quantity,
                          Condition condition,
                          MultipartFile imageFile);

    // Deletes a product only if it has no active orders.
    void deleteProduct(Long userId, Long productId);

    // Counts active listings (quantity > 0) for the seller.
    long countActiveListings(Long userId);

    // Resolves the approved seller id for the current user.
    Long getSellerIdForUser(Long userId);

    // Fetches featured products based on top orders for the homepage.
    java.util.List<Product> getFeaturedProducts(int limit);

    // Counts products in a category for the homepage cards.
    long countProductsByCategoryId(Long categoryId);

    // Loads a page of products for the explore page.
    org.springframework.data.domain.Page<Product> getProductPage(int pageNumber, int pageSize);

    // Loads a page of products for a category for the explore page.
    org.springframework.data.domain.Page<Product> getProductPageByCategory(Long categoryId, int pageNumber, int pageSize);

    // Searches products by title for the explore page.
    org.springframework.data.domain.Page<Product> searchProductPage(String query, int pageNumber, int pageSize);

    // Returns a small list of products for realtime search suggestions.
    java.util.List<Product> searchSuggestions(String query, int limit);

    // Fetches a single product with all its details (images, category, seller) for the product detail page.
    Product getProductDetail(Long productId);

    // Fetches related products from the same category excluding the current product.
    java.util.List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit);
}
