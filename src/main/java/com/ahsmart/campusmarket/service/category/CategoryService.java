package com.ahsmart.campusmarket.service.category;

import com.ahsmart.campusmarket.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Category create(String name, String description);

    Category update(Long id, String name, String description);

    /**
     * Deletes a category only if it has no products.
     */
    void deleteIfNoProducts(Long id);

    // Fetches the most recently created categories for the homepage.
    List<Category> getTopCategories(int limit);
}
