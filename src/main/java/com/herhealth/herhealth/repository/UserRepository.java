package com.herhealth.herhealth.repository;

import com.herhealth.herhealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
    void deleteByEmailIgnoreCase(String email);
}