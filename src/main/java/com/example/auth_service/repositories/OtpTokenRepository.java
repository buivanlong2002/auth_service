package com.example.auth_service.repositories;


import com.example.auth_service.model.OtpToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {

    Optional<OtpToken> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.email = :email")
    void deleteByEmail(@Param("email") String email);
}