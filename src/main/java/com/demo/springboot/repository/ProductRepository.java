package com.demo.springboot.repository;

import com.demo.springboot.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<Product> findByQuantityGreaterThan(Integer quantity);
    
    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice ORDER BY p.price ASC")
    List<Product> findAffordableProducts(@Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findInStockProducts();
}
