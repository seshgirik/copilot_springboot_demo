package com.demo.springboot.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.springboot.entity.Product;
import com.demo.springboot.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    public ResponseEntity<Page<Product>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        logger.info("🌐 REST: GET /api/products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getAllProducts(pageable);
        logger.info("🌐 REST: Returning {} products (page {} of {})", 
                   products.getNumberOfElements(), products.getNumber() + 1, products.getTotalPages());
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        
        logger.info("🌐 REST: GET /api/products/{}", id);
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            logger.info("🌐 REST: Returning product: {}", product.get().getName());
            return ResponseEntity.ok(product.get());
        } else {
            logger.warn("🌐 REST: Product not found, returning 404");
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<Product>> searchProducts(
            @Parameter(description = "Product name to search") @RequestParam String name) {
        
        logger.info("🌐 REST: GET /api/products/search?name={}", name);
        List<Product> products = productService.searchProductsByName(name);
        logger.info("🌐 REST: Returning {} products matching '{}'", products.size(), name);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range", description = "Retrieve products within a specific price range")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice) {
        
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/in-stock")
    @Operation(summary = "Get in-stock products", description = "Retrieve all products that are currently in stock")
    @ApiResponse(responseCode = "200", description = "In-stock products retrieved successfully")
    public ResponseEntity<List<Product>> getInStockProducts() {
        List<Product> products = productService.getProductsInStock();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/affordable")
    @Operation(summary = "Get affordable products", description = "Retrieve products under a maximum price")
    @ApiResponse(responseCode = "200", description = "Affordable products retrieved successfully")
    public ResponseEntity<List<Product>> getAffordableProducts(
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice) {
        
        List<Product> products = productService.getAffordableProducts(maxPrice);
        return ResponseEntity.ok(products);
    }
    
    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        logger.info("🌐 REST: POST /api/products - creating product: {}", product.getName());
        logger.debug("🌐 REST: Product data - Price: {}, Quantity: {}", product.getPrice(), product.getQuantity());
        Product createdProduct = productService.createProduct(product);
        logger.info("🌐 REST: Product created successfully with ID: {}", createdProduct.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody Product productDetails) {
        
        logger.info("🌐 REST: PUT /api/products/{} - updating product", id);
        logger.debug("🌐 REST: Update data - Name: '{}', Price: {}, Quantity: {}", 
                    productDetails.getName(), productDetails.getPrice(), productDetails.getQuantity());
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            logger.info("🌐 REST: Product updated successfully");
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            logger.warn("🌐 REST: Product update failed - not found, returning 404");
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update product stock", description = "Update the stock quantity of a product")
    @ApiResponse(responseCode = "200", description = "Stock updated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<?> updateProductStock(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "New quantity") @RequestParam Integer quantity) {
        
        logger.info("🌐 REST: PATCH /api/products/{}/stock - updating stock to {}", id, quantity);
        try {
            Product updatedProduct = productService.updateStock(id, quantity);
            logger.info("🌐 REST: Stock updated successfully");
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            logger.warn("🌐 REST: Stock update failed - product not found, returning 404");
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product by ID")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        
        logger.info("🌐 REST: DELETE /api/products/{}", id);
        try {
            productService.deleteProduct(id);
            logger.info("🌐 REST: Product deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("🌐 REST: Product deletion failed - not found, returning 404");
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/count")
    @Operation(summary = "Get product count", description = "Get total number of products")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Long> getProductCount() {
        long count = productService.getTotalCount();
        return ResponseEntity.ok(count);
    }
}
