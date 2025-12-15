package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.config.AppConstants;
import com.ahsmart.campusmarket.payloadDTOs.productSite.ProductDTO;
import com.ahsmart.campusmarket.payloadDTOs.productSite.ProductResponseDTO;
import com.ahsmart.campusmarket.service.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    // save a product
    @PostMapping("/api/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable Long categoryId)
    {
        ProductDTO savedProductDTO = productService.addProduct(productDTO, categoryId);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    //get all products
    @GetMapping("/api/public/products")
    public ResponseEntity<ProductResponseDTO> getALlProducts(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                             @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                             @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder)
    {
        ProductResponseDTO productResponse = productService.getAllProducts(pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // get products by category
    @GetMapping("/api/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponseDTO> getProductsByCategory(@PathVariable Long categoryId,
                                                                    @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                    @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                    @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                    @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder)
    {
        ProductResponseDTO productResponse = productService.getProductsByCategory(categoryId,pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // search products by keywords
    @GetMapping("/api/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponseDTO> getProductsByKeyword(@PathVariable String keyword,
                                                                   @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                   @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                   @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                   @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder)
    {
        ProductResponseDTO productResponse = productService.searchProductByKeyword(keyword,pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }


    // update product
    @PutMapping("/api/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable Long productId)
    {
        ProductDTO updatedProductDTO = productService.updateProduct(productDTO, productId);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }


    // delete product
    @DeleteMapping("/api/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId)
    {
        ProductDTO deletedProduct = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }

    // update image of product
    @PutMapping("/api/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam("image") MultipartFile image)
    {
        ProductDTO UpdatedProduct = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(UpdatedProduct, HttpStatus.OK);
    }
}
