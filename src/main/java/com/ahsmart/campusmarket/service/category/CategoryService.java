package com.ahsmart.campusmarket.service.category;

import com.ahsmart.campusmarket.payloadDTOs.CategoryDTO;
import com.ahsmart.campusmarket.payloadDTOs.CategoryResponseDTO;


public interface CategoryService {

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryResponseDTO getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);

    CategoryDTO deleteCategory(Long id);
}
