package com.demo.springboot.integration;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.demo.springboot.entity.Product;
import com.demo.springboot.entity.User;
import com.demo.springboot.repository.ProductRepository;
import com.demo.springboot.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for the Spring Boot application
 * Tests full end-to-end scenarios with real database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Application Integration Tests")
class ApplicationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up repositories
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("User API Integration Tests")
    class UserApiIntegrationTests {

        @Test
        @DisplayName("Should perform complete user CRUD operations")
        void shouldPerformCompleteUserCrudOperations() throws Exception {
            // Create user
            User newUser = new User();
            newUser.setName("Integration Test User");
            newUser.setEmail("integration@test.com");
            newUser.setPhone("123-456-7890");

            String userJson = objectMapper.writeValueAsString(newUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Integration Test User")))
                    .andExpect(jsonPath("$.email", is("integration@test.com")))
                    .andExpect(jsonPath("$.phone", is("123-456-7890")))
                    .andExpect(jsonPath("$.id").isNotEmpty());

            // Get all users
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Integration Test User")));

            // Get user by ID (assuming ID 1 for the first user)
            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Integration Test User")))
                    .andExpect(jsonPath("$.email", is("integration@test.com")));

            // Update user
            User updatedUser = new User();
            updatedUser.setName("Updated Test User");
            updatedUser.setEmail("updated@test.com");
            updatedUser.setPhone("999-888-7777");

            String updatedUserJson = objectMapper.writeValueAsString(updatedUser);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedUserJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Test User")))
                    .andExpect(jsonPath("$.email", is("updated@test.com")))
                    .andExpect(jsonPath("$.phone", is("999-888-7777")));

            // Delete user
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());

            // Verify user is deleted
            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle user validation errors")
        void shouldHandleUserValidationErrors() throws Exception {
            // Try to create user with invalid data
            User invalidUser = new User();
            invalidUser.setName(""); // Invalid: empty name
            invalidUser.setEmail("invalid-email"); // Invalid: not a valid email
            invalidUser.setPhone("123"); // Invalid: too short

            String invalidUserJson = objectMapper.writeValueAsString(invalidUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidUserJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should search users by name")
        void shouldSearchUsersByName() throws Exception {
            // Create test users
            User user1 = new User("John Doe", "john@test.com", "123-456-7890");
            User user2 = new User("Jane Smith", "jane@test.com", "987-654-3210");
            User user3 = new User("Bob Johnson", "bob@test.com", "555-123-4567");

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);

            // Search for users with "Jo" in name
            mockMvc.perform(get("/api/users/search")
                            .param("name", "Jo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder("John Doe", "Bob Johnson")));
        }
    }

    @Nested
    @DisplayName("Product API Integration Tests")
    class ProductApiIntegrationTests {

        @Test
        @DisplayName("Should perform complete product CRUD operations")
        void shouldPerformCompleteProductCrudOperations() throws Exception {
            // Create product
            Product newProduct = new Product();
            newProduct.setName("Integration Test Product");
            newProduct.setDescription("Test product for integration testing");
            newProduct.setPrice(new BigDecimal("99.99"));
            newProduct.setQuantity(10);

            String productJson = objectMapper.writeValueAsString(newProduct);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Integration Test Product")))
                    .andExpect(jsonPath("$.description", is("Test product for integration testing")))
                    .andExpect(jsonPath("$.price", is(99.99)))
                    .andExpect(jsonPath("$.quantity", is(10)))
                    .andExpect(jsonPath("$.id").isNotEmpty());

            // Get all products
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Integration Test Product")));

            // Get product by ID (assuming ID 1 for the first product)
            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Integration Test Product")))
                    .andExpect(jsonPath("$.price", is(99.99)));

            // Update product
            Product updatedProduct = new Product();
            updatedProduct.setName("Updated Test Product");
            updatedProduct.setDescription("Updated description");
            updatedProduct.setPrice(new BigDecimal("149.99"));
            updatedProduct.setQuantity(20);

            String updatedProductJson = objectMapper.writeValueAsString(updatedProduct);

            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedProductJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Test Product")))
                    .andExpect(jsonPath("$.price", is(149.99)))
                    .andExpect(jsonPath("$.quantity", is(20)));

            // Delete product
            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());

            // Verify product is deleted
            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle product validation errors")
        void shouldHandleProductValidationErrors() throws Exception {
            // Try to create product with invalid data
            Product invalidProduct = new Product();
            invalidProduct.setName(""); // Invalid: empty name
            invalidProduct.setDescription("Valid description");
            invalidProduct.setPrice(new BigDecimal("-10.00")); // Invalid: negative price
            invalidProduct.setQuantity(-5); // Invalid: negative quantity

            String invalidProductJson = objectMapper.writeValueAsString(invalidProduct);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidProductJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should search products by name")
        void shouldSearchProductsByName() throws Exception {
            // Create test products
            Product product1 = new Product("iPhone 15", "Latest Apple smartphone", new BigDecimal("999.99"), 10);
            Product product2 = new Product("Samsung Galaxy", "Android smartphone", new BigDecimal("899.99"), 15);
            Product product3 = new Product("MacBook Pro", "Apple laptop", new BigDecimal("2499.99"), 5);

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);

            // Search for products with "phone" in name
            mockMvc.perform(get("/api/products/search")
                            .param("name", "phone"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder("iPhone 15", "Samsung Galaxy")));
        }
    }

    @Nested
    @DisplayName("Application Health Tests")
    class ApplicationHealthTests {

        @Test
        @DisplayName("Should provide health check endpoint")
        void shouldProvideHealthCheckEndpoint() throws Exception {
            // Test basic health endpoint if available
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle 404 for non-existent endpoints")
        void shouldHandle404ForNonExistentEndpoints() throws Exception {
            mockMvc.perform(get("/api/nonexistent"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle method not allowed")
        void shouldHandleMethodNotAllowed() throws Exception {
            // Try to POST to a GET-only endpoint
            mockMvc.perform(post("/api/users/1"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Content Type and Format Tests")
    class ContentTypeAndFormatTests {

        @Test
        @DisplayName("Should accept and return JSON content type")
        void shouldAcceptAndReturnJsonContentType() throws Exception {
            User newUser = new User("JSON Test User", "json@test.com", "123-456-7890");
            String userJson = objectMapper.writeValueAsString(newUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            User newUser = new User("No Content Type User", "noct@test.com", "123-456-7890");
            String userJson = objectMapper.writeValueAsString(newUser);

            mockMvc.perform(post("/api/users")
                            .content(userJson)) // No content type specified
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}
