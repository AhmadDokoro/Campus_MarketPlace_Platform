package com.ahsmart.campusmarket.payloadDTOs.productSite;

import com.ahsmart.campusmarket.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String description;
    private String image;
    private Integer quantity;
    private double price;
    private double discount;
    private ProductStatus productStatus;


    // this will ensure SpecialPrice is always included in response but its not stored in the db at all it is derived when needed.
    public Double getSpecialPrice() {
        if (discount == 0.0) return price;
        return price - ((discount / 100) * price);
    }
}
