package com.example.auth_service.repositories;

import com.example.auth_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(String userId);

}
