package com.ahsmart.campusmarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @NotBlank
    @Size(min = 3, max = 100, message = "category name must be between 3 and 100 characters")
    @Column(name = "category_name", length = 100, nullable = false, unique = true)
    private String categoryName;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    // relate with product one to many
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();
}
