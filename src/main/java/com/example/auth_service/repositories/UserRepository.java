package com.example.auth_service.repositories;

import com.example.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    User getUserById(String userId);

    Optional<Object> findByPhone(String phone);
}
