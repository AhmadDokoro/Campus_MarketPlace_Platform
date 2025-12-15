package com.ahsmart.campusmarket.service.product;


import com.ahsmart.campusmarket.exceptions.APIException;
import com.ahsmart.campusmarket.exceptions.ResourceNotFoundException;
import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.payloadDTOs.productSite.ProductDTO;
import com.ahsmart.campusmarket.payloadDTOs.productSite.ProductResponseDTO;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.service.helper.HelperService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository; // final so that they most be initialized at object creation

    private final CategoryRepository categoryRepository;

    private final  ModelMapper modelMapper;

    private final FileService fileService;

    private final HelperService helperService;


    // create new product
    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId)
    {
        //get the category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));

        // check if product exist
        boolean isNotAvailable = category.getProducts().stream()
                .noneMatch(productValue -> productValue.getTitle().equals(productDTO.getProductName()) &&
                        productValue.getDescription ().equals(productDTO.getDescription())
                );


        if(!isNotAvailable)
        {
            throw new APIException("Product Already Exist!!");
        }

        Product product = modelMapper.map(productDTO, Product.class);

        // set the product's category
        product.setCategory(category);
        //product.setImage("Default.png");

        // set it status and calculate special price.
        //helperService.updateProductStatus(product);


        //save it
        Product savedproduct = productRepository.save(product);

        // convert to, and return productDTO
        return modelMapper.map(savedproduct, ProductDTO.class);

    }


    // get all products
    @Override
    public ProductResponseDTO getAllProducts(Integer pageNumber, Integer pageSize,String sortBy, String sortOrder)
    {
        //define sorting object with the data
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending() //sort ascending
                : Sort.by(sortBy).descending(); // sort descending

        // set our paging info by passing how the pages should be.
        Pageable pageDetail = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        Page<Product> pagedProduct = productRepository.findAll(pageDetail);

        // get all the products
        List<Product> products = pagedProduct.getContent();

        //check if no product and throw APIException
        if(products.isEmpty())
            throw new APIException("No products found!");

        // convert to list of productDTO
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // create Product response, set it, and return it.
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContents(productDTOs);
        productResponseDTO.setPageNumber(pagedProduct.getNumber());
        productResponseDTO.setPageSize(pagedProduct.getSize());
        productResponseDTO.setTotalElements(pagedProduct.getTotalElements());
        productResponseDTO.setTotalPages(pagedProduct.getTotalPages());
        productResponseDTO.setLastPage(pagedProduct.isLast());

        return productResponseDTO;
    }



    //get products by category
    @Override
    public ProductResponseDTO getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder)
    {
        //get the category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));


        //define sorting object with the data
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending() //sort ascending
                : Sort.by(sortBy).descending(); // sort descending

        // set our paging info by passing how the pages should be.
        Pageable pageDetail = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        Page<Product> pagedProduct = productRepository.findByCategoryOrderByPriceAsc(category, pageDetail);


        // get all the products
        List<Product> products = pagedProduct.getContent();


        //check if no product and throw APIException
        if(products.isEmpty())
            throw new APIException("No products found in this category!");

        // convert to list of productDTO
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // create Product response, set it, and return it.
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContents(productDTOs);
        productResponseDTO.setPageNumber(pagedProduct.getNumber());
        productResponseDTO.setPageSize(pagedProduct.getSize());
        productResponseDTO.setTotalElements(pagedProduct.getTotalElements());
        productResponseDTO.setTotalPages(pagedProduct.getTotalPages());
        productResponseDTO.setLastPage(pagedProduct.isLast());

        return productResponseDTO;
    }



    // search product by keyword
    @Override
    public ProductResponseDTO searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder)
    {
        //define sorting object with the data
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending() //sort ascending
                : Sort.by(sortBy).descending(); // sort descending

        // set our paging info by passing how the pages should be.
        Pageable pageDetail = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        Page<Product> pagedProduct = productRepository.findByTitleContainingIgnoreCase(keyword, pageDetail);


        // get all the products
        List<Product> products = pagedProduct.getContent();

        // validate
        if(products.isEmpty())
            throw new APIException("No products found with keyword: "+keyword);

        // convert to list of productDTO
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // create Product response, set it, and return it.
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContents(productDTOs);
        productResponseDTO.setPageNumber(pagedProduct.getNumber());
        productResponseDTO.setPageSize(pagedProduct.getSize());
        productResponseDTO.setTotalElements(pagedProduct.getTotalElements());
        productResponseDTO.setTotalPages(pagedProduct.getTotalPages());
        productResponseDTO.setLastPage(pagedProduct.isLast());

        return productResponseDTO;
    }


    // update product
    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId)
    {
        // get the product or throw exception
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        // convert to product to make changes and save
        Product product = modelMapper.map(productDTO, Product.class);

        // update the product with the product gotten from requestBody
        productFromDb.setTitle(product.getTitle()); // name
        productFromDb.setDescription(product.getDescription());// description
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setPrice(product.getPrice());


        // set it status and calculate special price.
        // helperService.updateProductStatus(productFromDb);

        //save it back to Db and
        Product updatedProduct = productRepository.save(productFromDb);

        //return the DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }


    // delete product
    @Override
    public ProductDTO deleteProduct(Long productId) {
        // get the product or throw exception
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        productRepository.delete(product);

        return modelMapper.map(product, ProductDTO.class);
    }


    // update image
    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) {

        // get the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        // Upload to Cloudinary
        String imageUrl = fileService.uploadImage(image);

        //set the url of the product image and save it back
        //product.setImage(imageUrl);
        Product updatedProduct = productRepository.save(product);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }


}
