package com.ahsmart.campusmarket.model;

import com.ahsmart.campusmarket.model.converter.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the AI scam-detection metadata for a single product.
 * <p>
 * The classification itself (VERIFIED / SUSPICIOUS / UNKNOWN) continues to live exclusively in
 * {@code products.flagged_status} — this table only holds the supporting confidence score and the
 * list of human-readable reasons. One product maps to at most one analysis row (unique product_id).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_ai_analysis",
       uniqueConstraints = @UniqueConstraint(name = "uk_ai_analysis_product", columnNames = "product_id"))
public class ProductAiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    // AI confidence in the stored flagged_status, expressed as a 0–100 percentage.
    @Column(name = "confidence_score")
    private Integer confidenceScore;

    // List of short reason strings, persisted as a JSON array in a single TEXT column.
    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "reasons", columnDefinition = "TEXT")
    private List<String> reasons = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
