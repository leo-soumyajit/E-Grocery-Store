package com.soumyajit.E_Grocery.Shop.Repository;

import com.soumyajit.E_Grocery.Shop.Entities.RationList;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RationListRepository extends JpaRepository<RationList, Long> {

    Optional<RationList> findByUser(User user);


    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<RationList> findAll();
    // <-- this will now fetch items + products eagerly
}
