package com.example.hay_mart.repositorys;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    List<User> findByStatusIgnoreCaseAndStarDateBefore(String status, LocalDate date);

    User findUsersByEmail(String email);
}
