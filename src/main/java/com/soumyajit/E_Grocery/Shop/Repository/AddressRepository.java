package com.soumyajit.E_Grocery.Shop.Repository;
import com.soumyajit.E_Grocery.Shop.Entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
}
