package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // we use this in getProductsByCategory to find the products in that category
    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetail);

    // used in searchProductByKeyword() to get all the products that has the keyword anywhere in it name.
    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageDetail);
}
