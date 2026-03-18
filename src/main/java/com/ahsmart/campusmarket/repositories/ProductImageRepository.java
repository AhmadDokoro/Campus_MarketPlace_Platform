package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Fetch the primary image for a product to render in the dashboard list.
    Optional<ProductImage> findFirstByProduct_ProductIdAndIsPrimaryTrue(Long productId);

    // Fetch all images to support deletion or image replacement.
    List<ProductImage> findByProduct_ProductId(Long productId);
}
