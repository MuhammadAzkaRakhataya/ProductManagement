package com.developer.product_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( name = "stock_product")
    private int StockProduct;

    @Column( name = "total_available")
    private int TotalAvailable;

    @Column( name = "total_defect")
    private int TotalDefect;

}
