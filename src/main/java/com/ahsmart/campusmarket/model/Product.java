package com.ahsmart.campusmarket.model;

import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    // seller relation (foreign key fk_product_seller) — mapped to Seller entity
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // category relation (foreign key fk_product_category) — mapped as ManyToOne for JPA
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "`condition`", length = 10)
    private Condition condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "flagged_status", columnDefinition = "ENUM('UNKNOWN','SUSPICIOUS','VERIFIED')")
    private FlaggedStatus flaggedStatus = FlaggedStatus.UNKNOWN;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // Convenience accessor for existing code that expects a numeric categoryId
    @Transient
    public Long getCategoryId() {
        return this.category != null ? this.category.getCategoryId() : null;
    }

    // Convenience accessor for existing code that expects a numeric sellerId
    @Transient
    public Long getSellerId() {
        return this.seller != null ? this.seller.getSellerId() : null;
    }
}
