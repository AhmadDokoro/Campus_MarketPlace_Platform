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
import com.ahsmart.campusmarket.repositories.CartItemRepository;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.ProductImageRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.openai.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CartItemRepository cartItemRepository;
    private final CategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;
    private final FileService fileService;
    private final OrderItemRepository orderItemRepository;
    private final OpenAiService openAiService;

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
                                 MultipartFile imageFile,
                                 String imageUrl) {

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
        boolean hasFile = imageFile != null && !imageFile.isEmpty();
        boolean hasUrl  = imageUrl != null && !imageUrl.isBlank();
        if (!hasFile && !hasUrl) {
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
        FlaggedStatus flaggedStatus = openAiService.detectFraud(normalizedTitle, description, price);
        product.setFlaggedStatus(flaggedStatus);

        Product savedProduct = productRepository.save(product);

        String uploadedUrl = hasFile
                ? fileService.uploadImage(imageFile)
                : fileService.uploadImageFromUrl(imageUrl);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(savedProduct);
        productImage.setImageUrl(uploadedUrl);
        productImage.setPublicId(extractPublicId(uploadedUrl));
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

    // Deletes only when the product is not part of order history; cart rows are removed first.
    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Seller seller = resolveApprovedSeller(userId);

        Product product = productRepository.findByProductIdAndSeller_SellerId(productId, seller.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found for this seller."));

        if (orderItemRepository.existsByProduct_ProductId(productId)) {
            throw new IllegalArgumentException("Cannot delete: product is tied to existing orders.");
        }

        List<String> imageUrls = getProductImageUrls(productId);
        cartItemRepository.deleteAllByProduct_ProductId(productId);
        productImageRepository.deleteAllByProduct_ProductId(productId);
        productRepository.delete(product);
        productRepository.flush();
        deleteImageFilesQuietly(imageUrls);
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

    private List<String> getProductImageUrls(Long productId) {
        return productImageRepository.findByProduct_ProductId(productId).stream()
                .map(ProductImage::getImageUrl)
                .filter(Objects::nonNull)
                .toList();
    }

    private void deleteImageFilesQuietly(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                fileService.deleteImageByUrl(imageUrl);
            } catch (RuntimeException ex) {
                log.warn("Product row deleted but image cleanup failed for {}", imageUrl, ex);
            }
        }
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
        
        // If top ordered items are less than the limit, fill the rest with recent products
        if (ordered.size() < limit) {
            int remaining = limit - ordered.size();
            List<Product> recentProducts = productRepository.findAllByOrderByCreatedAtDesc(
                    PageRequest.of(0, limit) // Fetch more to ensure we have enough to skip duplicates
            );
            
            for (Product rp : recentProducts) {
                if (ordered.size() >= limit) break;
                if (!byId.containsKey(rp.getProductId())) {
                    ordered.add(rp);
                }
            }
        }
        
        return ordered;
    }

    // Counts products in a category for the homepage cards.
    @Override
    public long countProductsByCategoryId(Long categoryId) {
        return productRepository.countByCategory_CategoryId(categoryId);
    }

    // Returns all category product counts in a single GROUP BY query.
    @Override
    public Map<Long, Long> getCategoryCountMap() {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : productRepository.countGroupedByCategory()) {
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    // Loads a paginated product list using two-phase ID-first query to avoid in-memory pagination.
    @Override
    public Page<Product> getProductPage(int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize));
        // Phase 1: fetch only IDs with correct SQL LIMIT/OFFSET.
        Page<Long> idPage = productRepository.findAllIds(pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }
        // Phase 2: fetch full products with images for only those IDs.
        List<Product> products = productRepository.findByProductIdInWithImages(idPage.getContent());
        // Re-order to match the original ID order.
        Map<Long, Product> byId = new HashMap<>();
        for (Product p : products) byId.put(p.getProductId(), p);
        List<Product> ordered = new ArrayList<>();
        for (Long id : idPage.getContent()) {
            Product p = byId.get(id);
            if (p != null) ordered.add(p);
        }
        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
    }

    // Loads a paginated product list for one category on the explore page.
    @Override
    public Page<Product> getProductPageByCategory(Long categoryId, int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize));
        // Phase 1: IDs only — SQL LIMIT is safe without any collection join.
        Page<Long> idPage = productRepository.findIdsByCategory(categoryId, pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }
        // Phase 2: load only the products on this page with their images.
        Map<Long, Product> byId = new HashMap<>();
        for (Product p : productRepository.findByProductIdInWithImages(idPage.getContent())) {
            byId.put(p.getProductId(), p);
        }
        List<Product> ordered = new ArrayList<>();
        for (Long id : idPage.getContent()) {
            Product p = byId.get(id);
            if (p != null) ordered.add(p);
        }
        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
    }

    // Searches products by title text with pagination for the explore page.
    @Override
    public Page<Product> searchProductPage(String searchText, int pageNumber, int pageSize) {
        String q = searchText == null ? "" : searchText.trim();
        PageRequest pageable = PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize));
        // Phase 1: IDs only — avoids in-memory pagination caused by collection joins.
        Page<Long> idPage = productRepository.findIdsByTitleContaining(q, pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }
        // Phase 2: load full products for only those IDs.
        Map<Long, Product> byId = new HashMap<>();
        for (Product p : productRepository.findByProductIdInWithImages(idPage.getContent())) {
            byId.put(p.getProductId(), p);
        }
        List<Product> ordered = new ArrayList<>();
        for (Long id : idPage.getContent()) {
            Product p = byId.get(id);
            if (p != null) ordered.add(p);
        }
        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
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
