package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Dung cho Login: tim User theo email de lay passwordHash ra so khop
    Optional<User> findByEmail(String email);
}
