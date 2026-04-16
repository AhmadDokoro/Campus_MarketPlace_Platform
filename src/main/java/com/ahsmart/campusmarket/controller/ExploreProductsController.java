package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductImage;
import com.ahsmart.campusmarket.service.category.CategoryService;
import com.ahsmart.campusmarket.service.product.ProductService;
import com.ahsmart.campusmarket.service.review.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ExploreProductsController {

    // Serves product browsing pages for buyers.
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    public ExploreProductsController(ProductService productService,
                                     CategoryService categoryService,
                                     ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    // Loads the main explore page with pagination and optional title search filter.
    @GetMapping("/products")
    public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "12") int size,
                               @RequestParam(name = "searchText", required = false) String searchText,
                               @RequestParam(name = "query", required = false) String legacyQuery,
                               Model model) {
        // Keep backward compatibility by accepting old query parameter then normalizing to searchText.
        String effectiveSearchText = (searchText != null && !searchText.isBlank()) ? searchText : legacyQuery;

        // Apply search filtering only when a non-empty search text is provided.
        if (effectiveSearchText != null && !effectiveSearchText.isBlank()) {
            model.addAttribute("page", productService.searchProductPage(effectiveSearchText, page, size));
            model.addAttribute("searchText", effectiveSearchText);
        } else {
            model.addAttribute("page", productService.getProductPage(page, size));
        }
        model.addAttribute("categories", categoryService.findAll());
        return "buyer/product-list";
    }

    // Loads the explore page filtered by the selected category.
    @GetMapping("/products/category/{categoryId}")
    public String listProductsByCategory(@PathVariable Long categoryId,
                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "12") int size,
                                         Model model) {
        // Return only products inside the selected category.
        model.addAttribute("page", productService.getProductPageByCategory(categoryId, page, size));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        return "buyer/product-list";
    }

    // Returns JSON suggestions for header live-search as the user types.
    @GetMapping("/products/search")
    @ResponseBody
    public List<SearchResult> searchProducts(@RequestParam(name = "searchText", required = false) String searchText,
                                             @RequestParam(name = "query", required = false) String legacyQuery) {
        // Keep backward compatibility by accepting old query parameter then normalizing to searchText.
        String effectiveSearchText = (searchText != null && !searchText.isBlank()) ? searchText : legacyQuery;

        // Map product entities to a lightweight payload for the dropdown list.
        List<Product> products = productService.searchSuggestions(effectiveSearchText, 8);
        List<SearchResult> results = new ArrayList<>();
        for (Product product : products) {
            results.add(new SearchResult(
                    product.getProductId(),
                    product.getTitle(),
                    product.getPrice(),
                    resolveSuggestionImageUrl(product)
            ));
        }
        return results;
    }

    // Loads the product detail page showing full product info and related products.
    @GetMapping("/products/{productId}")
    public String viewProductDetail(@PathVariable Long productId, Model model) {
        try {
            // Fetch the full product with images, category, and seller eagerly loaded.
            Product product = productService.getProductDetail(productId);
            model.addAttribute("product", product);
            model.addAttribute("productRating", reviewService.getProductRatingData(productId));
            model.addAttribute("productReviews", reviewService.getReviewsByProductId(productId));

            // Fetch up to 4 related products from the same category for "You May Also Like" section.
            List<Product> relatedProducts = productService.getRelatedProducts(
                    product.getCategory().getCategoryId(), product.getProductId(), 4);
            model.addAttribute("relatedProducts", relatedProducts);

            return "product-listings/product-detail";
        } catch (IllegalArgumentException e) {
            // Redirect to products list if the product is not found.
            return "redirect:/products";
        }
    }

    // Resolves image URL for suggestions: primary image first, then first non-empty image, else null.
    private String resolveSuggestionImageUrl(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        for (ProductImage image : product.getImages()) {
            if (Boolean.TRUE.equals(image.getIsPrimary()) && image.getImageUrl() != null && !image.getImageUrl().isBlank()) {
                return image.getImageUrl();
            }
        }

        for (ProductImage image : product.getImages()) {
            if (image.getImageUrl() != null && !image.getImageUrl().isBlank()) {
                return image.getImageUrl();
            }
        }

        return null;
    }

    // Lightweight response for search suggestions in the header.
    public record SearchResult(Long id, String title, BigDecimal price, String imageUrl) {}
}
