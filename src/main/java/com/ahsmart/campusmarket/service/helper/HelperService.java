package com.ahsmart.campusmarket.service.helper;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductStatus;
import org.springframework.stereotype.Service;

@Service
public class HelperService {


    // this is to be called from addProduct and update product to reduce the redundancy

    public void updateProductStatus(Product product) {
        // set product status
        if (product.getQuantity() <= 0) {
            product.setProductStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setProductStatus(ProductStatus.AVAILABLE);
        }

    }
}
