// Translated from: backend/app/repositories/user_repository.py
package com.example.bwms.repository;

import com.example.bwms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Python: email.lower().strip() — callers must lowercase+trim before calling
    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByNameAsc();
}
