package com.ahsmart.campusmarket.service.category;

import com.ahsmart.campusmarket.exceptions.APIException;
import com.ahsmart.campusmarket.exceptions.ResourceNotFoundException;
import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.payloadDTOs.CategoryDTO;
import com.ahsmart.campusmarket.payloadDTOs.CategoryResponseDTO;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class categoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    //create category
    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO)
    {
        Category verifyCategoryExistence = categoryRepository.findByCategoryName(categoryDTO.getCategoryName());
        //check it already exist
        if(verifyCategoryExistence != null)
        {
            throw new APIException("Category with category name:   "+ categoryDTO.getCategoryName() +" already exist!!");
        }

        // convert to entity type for saving
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);

        //convert to categoryDTO type for response
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }



    //get all category
    @Override
    public CategoryResponseDTO getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder)
    {
        //define sorting object with the data
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending() //sort ascending
                : Sort.by(sortBy).descending(); // sort descending

        // set our paging info by passing how the pages should be.
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> pageOfData = categoryRepository.findAll(pageDetails); //will return a list of data based on the page setting. but of page category type

        List<Category> categories = pageOfData.getContent(); // convert them to List of category.
        // check if it's empty
        if(categories.isEmpty())
        {
            throw new APIException("No category is found!!");
        }

        // convert the list gotten into categoryDTO object
        List<CategoryDTO> categoryDTO = categories.stream()
            .map(category -> modelMapper.map(category, CategoryDTO.class))
            .toList();

        // create a responseDTO object and set it data
        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setContents(categoryDTO);
        categoryResponseDTO.setPageNumber(pageOfData.getNumber());
        categoryResponseDTO.setPageSize(pageOfData.getSize());
        categoryResponseDTO.setTotalPages(pageOfData.getTotalPages());
        categoryResponseDTO.setTotalElements(pageOfData.getTotalElements());
        categoryResponseDTO.setLastPage(pageOfData.isLast());


        // return the Dto for response
        return categoryResponseDTO;
    }



    //update category
    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId)
    {
        //get the category using it id or throw exception if absent
        Category savedCategory = categoryRepository.findById(categoryId)   //custom exception i defined
                .orElseThrow(() -> new ResourceNotFoundException("Category","category name",categoryDTO.getCategoryName()));

        // convert the DTO to main chategory for saving
        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setCategoryId(categoryId); //set the id for updating purpose

        //save and convert the updatedCategory object it returns to CategoryDTO for return.
        return modelMapper.map(categoryRepository.save(category), CategoryDTO.class);
    }



    //delete category
    @Override
    public CategoryDTO deleteCategory(Long id)
    {
        //get the category using it id or throw exception if absent
        Category categoryToDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category id", id));

        //delete the category
        categoryRepository.delete(categoryToDelete);

        // convert the categoryToDelete object into categoryDTO and return it.
        return  modelMapper.map(categoryToDelete, CategoryDTO.class);

    }

}
