package com.example.hay_mart.repositorys;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    User findUsersByEmail(String email);
}
