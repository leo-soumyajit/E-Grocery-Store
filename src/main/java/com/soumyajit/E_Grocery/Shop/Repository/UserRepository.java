package com.soumyajit.E_Grocery.Shop.Repository;

import com.soumyajit.E_Grocery.Shop.Entities.Roles;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoles(String roles);
    List<User> findByRolesIgnoreCase(String roles);



}
