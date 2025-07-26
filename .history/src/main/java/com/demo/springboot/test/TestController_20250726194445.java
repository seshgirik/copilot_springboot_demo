package com.demo.springboot.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.springboot.service.ProductService;

/**
 * Simple test controller to verify basic compilation
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World - Compilation Test");
    }
    
    @GetMapping("/product-count")
    public ResponseEntity<Long> getCount() {
        long count = productService.getTotalCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/rate-limit")
    public ResponseEntity<String> testRateLimit() {
        return ResponseEntity.ok("Rate limit test endpoint - " + System.currentTimeMillis());
    }
}
