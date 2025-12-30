package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    // find by academic id (for uniqueness check)
    Optional<Users> findByAcademicId(String academicId);

}
