package com.demo.springboot.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.springboot.entity.Product;
import com.demo.springboot.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for ProductController
 * Tests all REST endpoints and HTTP responses
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product sampleProduct;
    private Product sampleProduct2;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setPrice(new BigDecimal("29.99"));
        sampleProduct.setQuantity(100);

        sampleProduct2 = new Product();
        sampleProduct2.setId(2L);
        sampleProduct2.setName("Another Product");
        sampleProduct2.setDescription("Another Description");
        sampleProduct2.setPrice(new BigDecimal("49.99"));
        sampleProduct2.setQuantity(50);
    }

    @Nested
    @DisplayName("Get All Products Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paged products successfully")
        void getAllProducts_ShouldReturnPagedProducts() throws Exception {
            // Given
            Page<Product> productPage = new PageImpl<>(
                Arrays.asList(sampleProduct, sampleProduct2), 
                PageRequest.of(0, 10), 
                2
            );
            when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

            // When & Then
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                    .andExpect(jsonPath("$.content[1].name").value("Another Product"))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @DisplayName("Should return products with custom pagination")
        void getAllProducts_WithCustomPagination_ShouldReturnPagedProducts() throws Exception {
            // Given
            Page<Product> productPage = new PageImpl<>(
                Arrays.asList(sampleProduct), 
                PageRequest.of(1, 1), 
                2
            );
            when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

            // When & Then
            mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.size").value(1));
        }
    }

    @Nested
    @DisplayName("Get Product By ID Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when product exists")
        void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
            // Given
            when(productService.getProductById(1L)).thenReturn(Optional.of(sampleProduct));

            // When & Then
            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.price").value(29.99))
                    .andExpect(jsonPath("$.quantity").value(100));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void getProductById_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
            // Given
            when(productService.getProductById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Search Products Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Should return products matching name search")
        void searchProducts_ByName_ShouldReturnMatchingProducts() throws Exception {
            // Given
            when(productService.searchProductsByName("Test")).thenReturn(Arrays.asList(sampleProduct));

            // When & Then
            mockMvc.perform(get("/api/products/search")
                        .param("name", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Product"));
        }

        @Test
        @DisplayName("Should return products in price range")
        void searchProducts_ByPriceRange_ShouldReturnProductsInRange() throws Exception {
            // Given
            when(productService.getProductsByPriceRange(new BigDecimal("20"), new BigDecimal("40")))
                    .thenReturn(Arrays.asList(sampleProduct));

            // When & Then
            mockMvc.perform(get("/api/products/search")
                        .param("minPrice", "20")
                        .param("maxPrice", "40"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].price").value(29.99));
        }

        @Test
        @DisplayName("Should return products in stock")
        void getProductsInStock_ShouldReturnInStockProducts() throws Exception {
            // Given
            when(productService.getProductsInStock()).thenReturn(Arrays.asList(sampleProduct, sampleProduct2));

            // When & Then
            mockMvc.perform(get("/api/products/in-stock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].quantity").value(100))
                    .andExpect(jsonPath("$[1].quantity").value(50));
        }

        @Test
        @DisplayName("Should return affordable products")
        void getAffordableProducts_ShouldReturnAffordableProducts() throws Exception {
            // Given
            when(productService.getAffordableProducts(new BigDecimal("35")))
                    .thenReturn(Arrays.asList(sampleProduct));

            // When & Then
            mockMvc.perform(get("/api/products/affordable")
                        .param("maxPrice", "35"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].price").value(29.99));
        }
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully with valid data")
        void createProduct_WithValidData_ShouldCreateProduct() throws Exception {
            // Given
            Product newProduct = new Product();
            newProduct.setName("New Product");
            newProduct.setDescription("New Description");
            newProduct.setPrice(new BigDecimal("39.99"));
            newProduct.setQuantity(75);

            Product savedProduct = new Product();
            savedProduct.setId(3L);
            savedProduct.setName("New Product");
            savedProduct.setDescription("New Description");
            savedProduct.setPrice(new BigDecimal("39.99"));
            savedProduct.setQuantity(75);

            when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

            // When & Then
            mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.name").value("New Product"))
                    .andExpect(jsonPath("$.price").value(39.99))
                    .andExpect(jsonPath("$.quantity").value(75));
        }

        @Test
        @DisplayName("Should return 400 for invalid product data")
        void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            // Given - Product with missing required fields
            Product invalidProduct = new Product();
            // Missing name and price

            // When & Then
            mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_WithValidData_ShouldUpdateProduct() throws Exception {
            // Given
            Product updateDetails = new Product();
            updateDetails.setName("Updated Product");
            updateDetails.setDescription("Updated Description");
            updateDetails.setPrice(new BigDecimal("35.99"));
            updateDetails.setQuantity(80);

            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Updated Product");
            updatedProduct.setDescription("Updated Description");
            updatedProduct.setPrice(new BigDecimal("35.99"));
            updatedProduct.setQuantity(80);

            when(productService.updateProduct(anyLong(), any(Product.class))).thenReturn(updatedProduct);

            // When & Then
            mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Updated Product"))
                    .andExpect(jsonPath("$.price").value(35.99))
                    .andExpect(jsonPath("$.quantity").value(80));
        }

        @Test
        @DisplayName("Should update product stock successfully")
        void updateProductStock_WithValidData_ShouldUpdateStock() throws Exception {
            // Given
            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Test Product");
            updatedProduct.setQuantity(150);

            when(productService.updateStock(1L, 150)).thenReturn(updatedProduct);

            // When & Then
            mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 150}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.quantity").value(150));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_WhenProductExists_ShouldDeleteProduct() throws Exception {
            // Given
            doNothing().when(productService).deleteProduct(1L);

            // When & Then
            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Product Count Tests")
    class ProductCountTests {

        @Test
        @DisplayName("Should return product count")
        void getProductCount_ShouldReturnCount() throws Exception {
            // Given
            when(productService.getTotalCount()).thenReturn(5L);

            // When & Then
            mockMvc.perform(get("/api/products/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }
    }
}
