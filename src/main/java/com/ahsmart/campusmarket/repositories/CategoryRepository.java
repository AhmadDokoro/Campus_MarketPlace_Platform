package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Category;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByCategoryName(@NotBlank String categoryName);

    // Returns latest categories for the homepage section.
    java.util.List<Category> findAllByOrderByCategoryIdDesc(org.springframework.data.domain.Pageable pageable);
}
