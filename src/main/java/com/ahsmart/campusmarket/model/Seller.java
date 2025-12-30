package com.ahsmart.campusmarket.model;

import com.ahsmart.campusmarket.model.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    @Column(name = "id_card_image_url", nullable = false, length = 500)
    private String idCardImageUrl;

    @Column(name = "mynemo_profile_url", length = 500)
    private String mynemoProfileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status = SellerStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Users reviewer;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
