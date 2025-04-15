package com.example.hay_mart.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findRoleByRoleName(String roleName);
}
