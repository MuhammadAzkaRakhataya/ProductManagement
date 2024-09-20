package com.developer.product_management.repository;

import com.developer.product_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByFileName(String fileName);

}
