package com.ahsmart.campusmarket.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(
        name = "mentors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mentor_email", columnNames = "mentor_email")
        }
)
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "mentor_name", nullable = false, length = 150)
    private String mentorName;

    @Column(name = "mentor_email", nullable = false, length = 255)
    private String mentorEmail;
}

