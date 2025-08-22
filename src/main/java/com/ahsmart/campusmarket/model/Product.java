package com.ahsmart.campusmarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @Size(min = 3, message = "product name must be at least 3 characters")
    @NotBlank
    private String productName;
    private String image;

    @NotBlank
    @Size(min = 6, message = "product description must be at least 6 characters")
    private String description;
    private Integer quantity;
    private double price;
    private double discount;


    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    //relate product with category many to One
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
