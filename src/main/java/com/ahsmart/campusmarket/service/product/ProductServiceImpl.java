package com.ahsmart.campusmarket.service.product;


import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductImage;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.ProductImageRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;
    private final FileService fileService;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;

    // Validates input, uploads the image, and persists product + primary image.
    @Override
    @Transactional
    public Product createProduct(Long userId,
                                 Long categoryId,
                                 String title,
                                 String description,
                                 BigDecimal price,
                                 Integer quantity,
                                 Condition condition,
                                 MultipartFile imageFile) {

        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in to add products.");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Product title is required.");
        }
        String normalizedTitle = title.trim();
        if (normalizedTitle.length() < 3 || normalizedTitle.length() > 255) {
            throw new IllegalArgumentException("Title must be between 3 and 255 characters.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition is required.");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("A product image is required.");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Seller profile not found."));

        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new IllegalArgumentException("Seller is not approved yet.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle(normalizedTitle);
        product.setDescription(description == null || description.isBlank() ? null : description.trim());
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCondition(condition);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);

        Product savedProduct = productRepository.save(product);

        String imageUrl = fileService.uploadImage(imageFile);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(savedProduct);
        productImage.setImageUrl(imageUrl);
        productImage.setPublicId(extractPublicId(imageUrl));
        productImage.setIsPrimary(true);

        productImageRepository.save(productImage);

        savedProduct.getImages().add(productImage);
        return savedProduct;
    }



    // Loads all products belonging to the seller for the dashboard list.
    @Override
    public List<Product> getProductsForSeller(Long userId) {
        Seller seller = resolveApprovedSeller(userId);
        return productRepository.findBySeller_SellerId(seller.getSellerId());
    }

    // Ensures the seller owns the product before editing.
    @Override
    public Product getProductForEdit(Long userId, Long productId) {
        Seller seller = resolveApprovedSeller(userId);
        return productRepository.findByProductIdAndSeller_SellerId(productId, seller.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found for this seller."));
    }

    // Updates fields and optionally replaces the primary image.
    @Override
    @Transactional
    public Product updateProduct(Long userId,
                                 Long productId,
                                 Long categoryId,
                                 String title,
                                 String description,
                                 BigDecimal price,
                                 Integer quantity,
                                 Condition condition,
                                 MultipartFile imageFile) {
        Seller seller = resolveApprovedSeller(userId);

        Product product = productRepository.findByProductIdAndSeller_SellerId(productId, seller.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found for this seller."));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Product title is required.");
        }
        String normalizedTitle = title.trim();
        if (normalizedTitle.length() < 3 || normalizedTitle.length() > 255) {
            throw new IllegalArgumentException("Title must be between 3 and 255 characters.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition is required.");
        }

        product.setCategory(category);
        product.setTitle(normalizedTitle);
        product.setDescription(description == null || description.isBlank() ? null : description.trim());
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCondition(condition);

        if (imageFile != null && !imageFile.isEmpty()) {
            replacePrimaryImage(product, imageFile);
        }

        return productRepository.save(product);
    }

    // Deletes only when there are no active orders for this product.
    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Seller seller = resolveApprovedSeller(userId);

        Product product = productRepository.findByProductIdAndSeller_SellerId(productId, seller.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found for this seller."));

        if (orderService.hasActiveOrdersForProduct(productId)) {
            throw new IllegalArgumentException("Cannot delete: product is tied to active orders.");
        }

        deleteAllImages(productId);
        productRepository.delete(product);
    }

    // Counts active listings using quantity > 0.
    @Override
    public long countActiveListings(Long userId) {
        return getProductsForSeller(userId).stream()
                .filter(product -> product.getQuantity() != null && product.getQuantity() > 0)
                .count();
    }

    // Resolves the seller from the user and ensures approval.
    private Seller resolveApprovedSeller(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in to manage products.");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Seller profile not found."));

        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new IllegalArgumentException("Seller is not approved yet.");
        }

        return seller;
    }

    // Resolves seller id for dashboard stats.
    @Override
    public Long getSellerIdForUser(Long userId) {
        return resolveApprovedSeller(userId).getSellerId();
    }

    // Replaces the primary image on a product.
    private void replacePrimaryImage(Product product, MultipartFile imageFile) {
        productImageRepository.findFirstByProduct_ProductIdAndIsPrimaryTrue(product.getProductId())
                .ifPresent(existing -> {
                    fileService.deleteImageByUrl(existing.getImageUrl());
                    productImageRepository.delete(existing);
                });

        String imageUrl = fileService.uploadImage(imageFile);
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(imageUrl);
        productImage.setPublicId(extractPublicId(imageUrl));
        productImage.setIsPrimary(true);
        productImageRepository.save(productImage);
    }

    // Removes all product images (used before delete).
    private void deleteAllImages(Long productId) {
        productImageRepository.findByProduct_ProductId(productId)
                .forEach(image -> {
                    fileService.deleteImageByUrl(image.getImageUrl());
                    productImageRepository.delete(image);
                });
    }

    private String extractPublicId(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        String[] parts = imageUrl.split("/");
        if (parts.length == 0) {
            return null;
        }
        String fileName = parts[parts.length - 1];
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    // Fetches featured products ranked by order quantities, with latest-products fallback.
    @Override
    public List<Product> getFeaturedProducts(int limit) {
        // Exclude cancelled/refunded orders when calculating top ordered products.
        OrderStatus[] excluded = new OrderStatus[]{
                OrderStatus.CANCELLED,
                OrderStatus.REFUNDED
        };

        List<Long> topIds = orderItemRepository.findTopProductIdsByOrderQuantity(
                excluded,
                PageRequest.of(0, Math.max(1, limit))
        );

        if (topIds.isEmpty()) {
            // Fallback to most recent products when there are no orders yet.
            return productRepository.findAllByOrderByCreatedAtDesc(
                    PageRequest.of(0, Math.max(1, limit))
            );
        }

        List<Product> products = productRepository.findByProductIdInWithImages(topIds);
        // Preserve order based on the topIds ranking.
        Map<Long, Product> byId = new HashMap<>();
        for (Product product : products) {
            byId.put(product.getProductId(), product);
        }
        List<Product> ordered = new ArrayList<>();
        for (Long id : topIds) {
            Product product = byId.get(id);
            if (product != null) {
                ordered.add(product);
            }
        }
        return ordered;
    }

    // Counts products in a category for the homepage cards.
    @Override
    public long countProductsByCategoryId(Long categoryId) {
        return productRepository.countByCategory_CategoryId(categoryId);
    }

    // Loads a paginated product list for the explore page.
    @Override
    public Page<Product> getProductPage(int pageNumber, int pageSize) {
        // Build pageable safely with non-negative page and minimum size of 1.
        return productRepository.findAllBy(PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize)));
    }

    // Loads a paginated product list for one category on the explore page.
    @Override
    public Page<Product> getProductPageByCategory(Long categoryId, int pageNumber, int pageSize) {
        // Build pageable safely with non-negative page and minimum size of 1.
        return productRepository.findByCategory_CategoryId(
                categoryId,
                PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize))
        );
    }

    // Searches products by title text with pagination for the explore page.
    @Override
    public Page<Product> searchProductPage(String searchText, int pageNumber, int pageSize) {
        // Normalize the search text and return a paginated, case-insensitive title match.
        String normalizedSearchText = searchText == null ? "" : searchText.trim();
        return productRepository.findByTitleContainingIgnoreCase(
                normalizedSearchText,
                PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize))
        );
    }

    // Returns a small suggestion list for the header live-search dropdown.
    @Override
    public List<Product> searchSuggestions(String searchText, int limit) {
        // Return empty result when user has not typed search text yet.
        String normalizedSearchText = searchText == null ? "" : searchText.trim();
        if (normalizedSearchText.isEmpty()) {
            return Collections.emptyList();
        }
        Page<Product> page = productRepository.findByTitleContainingIgnoreCase(
                normalizedSearchText,
                PageRequest.of(0, Math.max(1, limit))
        );
        return page.getContent();
    }

    // Loads a single product with images, category, and seller details for the product detail page.
    @Override
    public Product getProductDetail(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        // Fetch product with all relationships eagerly loaded to avoid lazy loading issues in the template.
        return productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
    }

    // Fetches up to 'limit' products in the same category, excluding the current product for "You May Also Like".
    @Override
    public List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit) {
        if (categoryId == null || excludeProductId == null) {
            return Collections.emptyList();
        }
        // Query related products from the same category, limited to the requested count.
        return productRepository.findRelatedProducts(categoryId, excludeProductId, PageRequest.of(0, Math.max(1, limit)));
    }
}
