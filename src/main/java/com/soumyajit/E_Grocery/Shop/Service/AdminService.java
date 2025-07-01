package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@RequiredArgsConstructor
@Slf4j
@Service
public class AdminService {
    private final UserRepository userRepository;



    public void makeUserAdmin(String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the current user has ADMIN role
        if (authentication == null || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new SecurityException("Access denied: Only ADMINs can promote users.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User with this email not found"));

        log.info("Making user admin with email: {}", email);
        user.setRoles("ADMIN");
        userRepository.save(user);
    }



}
