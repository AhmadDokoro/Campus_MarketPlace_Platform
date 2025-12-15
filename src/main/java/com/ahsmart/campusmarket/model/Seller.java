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
    @JoinColumn(name = "user_id", unique = true)
    private Users user;

    @Column(name = "id_card_image_url")
    private String idCardImageUrl;

    @Column(name = "mynemo_profile_url")
    private String mynemoProfileUrl;

    @Enumerated(EnumType.STRING)
    private SellerStatus status;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Users reviewer;

    @Column(
            name = "submitted_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime submittedAt;
}

