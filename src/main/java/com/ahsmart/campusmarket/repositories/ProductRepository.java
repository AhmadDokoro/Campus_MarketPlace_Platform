package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Returns all products that belong to one seller for the seller dashboard table.
    List<Product> findBySeller_SellerId(Long sellerId);

    // Returns one product only if it belongs to the specified seller (ownership guard).
    Optional<Product> findByProductIdAndSeller_SellerId(Long productId, Long sellerId);

    // Counts products for one category to display category product totals.
    long countByCategory_CategoryId(Long categoryId);

    // Loads products by ids and fetches related images/category for featured rendering.
    @Query("select distinct p from Product p " +
            "left join fetch p.images " +
            "left join fetch p.category " +
            "where p.productId in :ids")
    List<Product> findByProductIdInWithImages(@Param("ids") List<Long> ids);

    // Loads latest created products with related images/category for fallback featured list.
    @EntityGraph(attributePaths = {"images", "category"})
    List<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Loads a paginated product list with related images/category for explore page.
    @EntityGraph(attributePaths = {"images", "category"})
    Page<Product> findAllBy(Pageable pageable);

    // Loads a paginated product list for a single category on explore page.
    @EntityGraph(attributePaths = {"images", "category"})
    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);

    // Searches products by title text (case-insensitive) with pagination.
    @EntityGraph(attributePaths = {"images", "category"})
    Page<Product> findByTitleContainingIgnoreCase(String searchText, Pageable pageable);

    // Fetches a single product by ID with images, category, and seller eagerly loaded for the detail page.
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller s " +
            "LEFT JOIN FETCH s.user " +
            "WHERE p.productId = :productId")
    Optional<Product> findByIdWithDetails(@Param("productId") Long productId);

    // Fetches related products from the same category excluding the current product for "You May Also Like" section.
    @EntityGraph(attributePaths = {"images", "category"})
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.productId <> :excludeId")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId,
                                      @Param("excludeId") Long excludeId,
                                      Pageable pageable);
}
