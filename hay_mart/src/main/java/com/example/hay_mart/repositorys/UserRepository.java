package com.example.hay_mart.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.User;

public interface UserRepository extends JpaRepository<User, Integer>{
    
}
