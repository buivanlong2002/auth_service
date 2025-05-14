package com.example.auth_service.repositories;


import com.example.auth_service.entity.UserPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, String> {
    List<UserPasswordHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}