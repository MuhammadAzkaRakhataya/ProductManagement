package com.developer.product_management;

import com.developer.product_management.service.ProductService;
import com.developer.product_management.service.impl.ProductServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.nio.file.FileStore;
import java.util.Arrays;

@SpringBootApplication
public class ProductManagementApplication {


	public static void main(String[] args) {SpringApplication.run(ProductManagementApplication.class, args);}



}
