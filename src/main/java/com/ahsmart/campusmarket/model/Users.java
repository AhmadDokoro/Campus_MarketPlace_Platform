package com.ahsmart.campusmarket.model;

import com.ahsmart.campusmarket.model.enums.Role;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name = "user_password")
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;
    private String phone;

    @Column(name = "academic_id")
    private String academicId;
    private String level;

    @Column(name = "mentor_name")
    private String mentorName;

    @Column(name = "mentor_email")
    private String mentorEmail;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}

