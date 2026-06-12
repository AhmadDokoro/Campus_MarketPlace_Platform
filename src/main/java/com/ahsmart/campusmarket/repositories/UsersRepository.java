package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmailAndUserIdNot(String email, Long userId);

    // case-insensitive duplicate checks used by registration (run BEFORE any save)
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByAcademicIdIgnoreCase(String academicId);

    // find by academic id (for uniqueness check)
    Optional<Users> findByAcademicId(String academicId);

    boolean existsByAcademicIdAndUserIdNot(String academicId, Long userId);

    // used to prevent deleting mentors that are assigned to users
    long countByMentorMentorId(Long mentorId);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

}
