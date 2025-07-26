package com.demo.springboot.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.demo.springboot.entity.Product;
import com.demo.springboot.repository.ProductRepository;

@Service
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        logger.info("🔍 Service: Getting all products");
        List<Product> products = productRepository.findAll();
        logger.info("✅ Service: Found {} products", products.size());
        return products;
    }
    
    public Page<Product> getAllProducts(Pageable pageable) {
        logger.info("🔍 Service: Getting products with pagination - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        logger.info("✅ Service: Found {} products (page {} of {})", 
                   products.getNumberOfElements(), products.getNumber() + 1, products.getTotalPages());
        return products;
    }
    
    public Optional<Product> getProductById(Long id) {
        logger.info("🔍 Service: Getting product by ID: {}", id);
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            logger.info("✅ Service: Found product: {}", product.get().getName());
        } else {
            logger.warn("❌ Service: Product not found with ID: {}", id);
        }
        return product;
    }
    
    public List<Product> searchProductsByName(String name) {
        logger.info("🔍 Service: Searching products by name containing: '{}'", name);
        logger.debug("🔍 Service: Executing case-insensitive name search query");
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        logger.info("✅ Service: Found {} products matching name '{}'", products.size(), name);
        if (logger.isDebugEnabled() && !products.isEmpty()) {
            products.forEach(p -> logger.debug("📦 Found product: ID={}, Name='{}', Price={}", 
                                             p.getId(), p.getName(), p.getPrice()));
        }
        return products;
    }
    
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.info("🔍 Service: Getting products in price range: {} - {}", minPrice, maxPrice);
        logger.debug("🔍 Service: Executing price range query with bounds validation");
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        logger.info("✅ Service: Found {} products in price range {} - {}", products.size(), minPrice, maxPrice);
        if (logger.isDebugEnabled() && !products.isEmpty()) {
            products.forEach(p -> logger.debug("💰 Product in range: ID={}, Name='{}', Price={}", 
                                             p.getId(), p.getName(), p.getPrice()));
        }
        return products;
    }
    
    public List<Product> getProductsInStock() {
        logger.info("🔍 Service: Getting products currently in stock");
        logger.debug("🔍 Service: Executing in-stock query (quantity > 0)");
        List<Product> products = productRepository.findInStockProducts();
        logger.info("✅ Service: Found {} products in stock", products.size());
        if (logger.isDebugEnabled() && !products.isEmpty()) {
            products.forEach(p -> logger.debug("📦 In-stock product: ID={}, Name='{}', Quantity={}", 
                                             p.getId(), p.getName(), p.getQuantity()));
        }
        return products;
    }
    
    public List<Product> getAffordableProducts(BigDecimal maxPrice) {
        logger.info("🔍 Service: Getting affordable products under: {}", maxPrice);
        logger.debug("🔍 Service: Executing affordable products query");
        List<Product> products = productRepository.findAffordableProducts(maxPrice);
        logger.info("✅ Service: Found {} affordable products under {}", products.size(), maxPrice);
        if (logger.isDebugEnabled() && !products.isEmpty()) {
            products.forEach(p -> logger.debug("💵 Affordable product: ID={}, Name='{}', Price={}", 
                                             p.getId(), p.getName(), p.getPrice()));
        }
        return products;
    }
    
    public Product createProduct(Product product) {
        logger.info("🆕 Service: Creating new product: {}", product.getName());
        logger.debug("🔍 Product details - Price: {}, Quantity: {}", product.getPrice(), product.getQuantity());
        Product savedProduct = productRepository.save(product);
        logger.info("✅ Service: Product created with ID: {}", savedProduct.getId());
        return savedProduct;
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        logger.info("🔄 Service: Updating product with ID: {}", id);
        logger.debug("🔍 Service: Searching for product to update");
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Service: Product not found for update with ID: {}", id);
                    return new RuntimeException("Product not found with id: " + id);
                });
        
        logger.debug("📋 Service: Current product details - Name: '{}', Price: {}, Quantity: {}", 
                    product.getName(), product.getPrice(), product.getQuantity());
        logger.debug("📝 Service: New product details - Name: '{}', Price: {}, Quantity: {}", 
                    productDetails.getName(), productDetails.getPrice(), productDetails.getQuantity());
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());
        
        logger.debug("💾 Service: Saving updated product to database");
        Product updatedProduct = productRepository.save(product);
        logger.info("✅ Service: Product updated successfully - ID: {}, Name: '{}'", 
                   updatedProduct.getId(), updatedProduct.getName());
        
        return updatedProduct;
    }
    
    public void deleteProduct(Long id) {
        logger.info("🗑️ Service: Deleting product with ID: {}", id);
        logger.debug("🔍 Service: Searching for product to delete");
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Service: Product not found for deletion with ID: {}", id);
                    return new RuntimeException("Product not found with id: " + id);
                });
        
        logger.debug("📋 Service: Found product to delete - Name: '{}', Price: {}", 
                    product.getName(), product.getPrice());
        logger.debug("💾 Service: Executing delete operation");
        
        productRepository.delete(product);
        logger.info("✅ Service: Product deleted successfully - ID: {}, Name: '{}'", id, product.getName());
    }
    
    public Product updateStock(Long id, Integer quantity) {
        logger.info("📦 Service: Updating stock for product ID: {} to quantity: {}", id, quantity);
        logger.debug("🔍 Service: Searching for product to update stock");
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Service: Product not found for stock update with ID: {}", id);
                    return new RuntimeException("Product not found with id: " + id);
                });
        
        Integer oldQuantity = product.getQuantity();
        logger.debug("📊 Service: Current stock quantity: {}, New quantity: {}", oldQuantity, quantity);
        
        product.setQuantity(quantity);
        
        logger.debug("💾 Service: Saving stock update to database");
        Product updatedProduct = productRepository.save(product);
        logger.info("✅ Service: Stock updated successfully - ID: {}, Old: {}, New: {}", 
                   id, oldQuantity, quantity);
        
        return updatedProduct;
    }
    
    public long getTotalCount() {
        logger.info("🔍 Service: Getting total product count");
        long count = productRepository.count();
        logger.info("✅ Service: Total products count: {}", count);
        return count;
    }
}
