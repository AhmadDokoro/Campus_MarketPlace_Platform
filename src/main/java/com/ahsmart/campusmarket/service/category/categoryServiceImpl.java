package com.ahsmart.campusmarket.service.category;


import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class categoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public categoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll(SortHelpers.byNameAsc());
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Category create(String name, String description) {
        String normalized = normalizeName(name);

        if (categoryRepository.findByCategoryName(normalized) != null) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category c = new Category();
        c.setCategoryName(normalized);
        c.setDescription(normalizeDescription(description));

        try {
            return categoryRepository.save(c);
        } catch (DataIntegrityViolationException dive) {
            throw new IllegalArgumentException("Category name already exists");
        }
    }

    @Override
    @Transactional
    public Category update(Long id, String name, String description) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String normalized = normalizeName(name);
        Category dup = categoryRepository.findByCategoryName(normalized);
        if (dup != null && !dup.getCategoryId().equals(id)) {
            throw new IllegalArgumentException("Category name already exists");
        }

        existing.setCategoryName(normalized);
        existing.setDescription(normalizeDescription(description));

        try {
            return categoryRepository.save(existing);
        } catch (DataIntegrityViolationException dive) {
            throw new IllegalArgumentException("Category name already exists");
        }
    }

    @Override
    @Transactional
    public void deleteIfNoProducts(Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Safety: if referenced by products, don't delete.
        if (existing.getProducts() != null && !existing.getProducts().isEmpty()) {
            throw new IllegalArgumentException("This category has products. Remove/move products before deleting.");
        }

        categoryRepository.delete(existing);
    }

    @Override
    public List<Category> getTopCategories(int limit) {
        // Get the most recent categories by id to keep the homepage fresh.
        return categoryRepository.findAllByOrderByCategoryIdDesc(
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit))
        );
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3 || trimmed.length() > 100) {
            throw new IllegalArgumentException("Category name must be between 3 and 100 characters");
        }
        return trimmed;
    }

    private String normalizeDescription(String description) {
        if (description == null) return null;
        String trimmed = description.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Description must be 255 characters or less");
        }
        return trimmed;
    }

    /** Small local helper to avoid touching wider style/utility code. */
    private static final class SortHelpers {
        private static org.springframework.data.domain.Sort byNameAsc() {
            return org.springframework.data.domain.Sort.by("categoryName").ascending();
        }
    }
}
