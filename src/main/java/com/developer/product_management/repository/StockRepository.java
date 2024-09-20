package com.developer.product_management.repository;

import com.developer.product_management.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StockRepository extends JpaRepository< Stock, Long>{

}
