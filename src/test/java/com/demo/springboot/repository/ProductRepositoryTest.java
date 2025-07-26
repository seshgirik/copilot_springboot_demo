package com.demo.springboot.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.demo.springboot.entity.Product;

/**
 * Unit tests for ProductRepository
 * Tests JPA repository methods and custom queries
 */
@DataJpaTest
@DisplayName("ProductRepository Unit Tests")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product sampleProduct1;
    private Product sampleProduct2;
    private Product sampleProduct3;

    @BeforeEach
    void setUp() {
        sampleProduct1 = new Product();
        sampleProduct1.setName("iPhone 15");
        sampleProduct1.setDescription("Latest Apple smartphone");
        sampleProduct1.setPrice(new BigDecimal("999.99"));
        sampleProduct1.setQuantity(50);

        sampleProduct2 = new Product();
        sampleProduct2.setName("Samsung Galaxy S24");
        sampleProduct2.setDescription("Android smartphone with great camera");
        sampleProduct2.setPrice(new BigDecimal("899.99"));
        sampleProduct2.setQuantity(30);

        sampleProduct3 = new Product();
        sampleProduct3.setName("MacBook Pro");
        sampleProduct3.setDescription("Professional laptop from Apple");
        sampleProduct3.setPrice(new BigDecimal("2499.99"));
        sampleProduct3.setQuantity(15);
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save product successfully")
        void shouldSaveProductSuccessfully() {
            // Act
            Product savedProduct = productRepository.save(sampleProduct1);

            // Assert
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getId()).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo("iPhone 15");
            assertThat(savedProduct.getDescription()).isEqualTo("Latest Apple smartphone");
            assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("999.99"));
            assertThat(savedProduct.getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should find product by ID successfully")
        void shouldFindProductByIdSuccessfully() {
            // Arrange
            Product persistedProduct = entityManager.persistAndFlush(sampleProduct1);
            Long productId = persistedProduct.getId();

            // Act
            Optional<Product> foundProduct = productRepository.findById(productId);

            // Assert
            assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getName()).isEqualTo("iPhone 15");
            assertThat(foundProduct.get().getDescription()).isEqualTo("Latest Apple smartphone");
            assertThat(foundProduct.get().getPrice()).isEqualTo(new BigDecimal("999.99"));
            assertThat(foundProduct.get().getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should return empty when product not found by ID")
        void shouldReturnEmptyWhenProductNotFoundById() {
            // Act
            Optional<Product> foundProduct = productRepository.findById(999L);

            // Assert
            assertThat(foundProduct).isEmpty();
        }

        @Test
        @DisplayName("Should find all products successfully")
        void shouldFindAllProductsSuccessfully() {
            // Arrange
            entityManager.persist(sampleProduct1);
            entityManager.persist(sampleProduct2);
            entityManager.persist(sampleProduct3);
            entityManager.flush();

            // Act
            List<Product> products = productRepository.findAll();

            // Assert
            assertThat(products).hasSize(3);
            assertThat(products).extracting(Product::getName)
                    .containsExactlyInAnyOrder("iPhone 15", "Samsung Galaxy S24", "MacBook Pro");
        }

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            // Arrange
            Product persistedProduct = entityManager.persistAndFlush(sampleProduct1);
            Long productId = persistedProduct.getId();

            // Act
            productRepository.deleteById(productId);
            entityManager.flush();

            // Assert
            Optional<Product> deletedProduct = productRepository.findById(productId);
            assertThat(deletedProduct).isEmpty();
        }

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Arrange
            Product persistedProduct = entityManager.persistAndFlush(sampleProduct1);
            Long productId = persistedProduct.getId();

            // Act
            persistedProduct.setName("iPhone 15 Pro");
            persistedProduct.setDescription("Updated iPhone model");
            persistedProduct.setPrice(new BigDecimal("1199.99"));
            persistedProduct.setQuantity(25);
            Product updatedProduct = productRepository.save(persistedProduct);
            entityManager.flush();

            // Assert
            assertThat(updatedProduct.getId()).isEqualTo(productId);
            assertThat(updatedProduct.getName()).isEqualTo("iPhone 15 Pro");
            assertThat(updatedProduct.getDescription()).isEqualTo("Updated iPhone model");
            assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("1199.99"));
            assertThat(updatedProduct.getQuantity()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should find products with pagination")
        void shouldFindProductsWithPagination() {
            // Arrange
            entityManager.persist(sampleProduct1);
            entityManager.persist(sampleProduct2);
            entityManager.persist(sampleProduct3);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 2);

            // Act
            Page<Product> productPage = productRepository.findAll(pageable);

            // Assert
            assertThat(productPage.getContent()).hasSize(2);
            assertThat(productPage.getTotalElements()).isEqualTo(3);
            assertThat(productPage.getTotalPages()).isEqualTo(2);
            assertThat(productPage.isFirst()).isTrue();
            assertThat(productPage.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should find second page of products")
        void shouldFindSecondPageOfProducts() {
            // Arrange
            entityManager.persist(sampleProduct1);
            entityManager.persist(sampleProduct2);
            entityManager.persist(sampleProduct3);
            entityManager.flush();

            Pageable pageable = PageRequest.of(1, 2);

            // Act
            Page<Product> productPage = productRepository.findAll(pageable);

            // Assert
            assertThat(productPage.getContent()).hasSize(1);
            assertThat(productPage.getTotalElements()).isEqualTo(3);
            assertThat(productPage.getTotalPages()).isEqualTo(2);
            assertThat(productPage.isLast()).isTrue();
            assertThat(productPage.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should return empty page when no products exist")
        void shouldReturnEmptyPageWhenNoProductsExist() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Product> productPage = productRepository.findAll(pageable);

            // Assert
            assertThat(productPage.getContent()).isEmpty();
            assertThat(productPage.getTotalElements()).isEqualTo(0);
            assertThat(productPage.getTotalPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find products by name containing")
        void shouldFindProductsByNameContaining() {
            // Arrange
            entityManager.persist(sampleProduct1); // iPhone 15
            entityManager.persist(sampleProduct2); // Samsung Galaxy S24
            entityManager.persist(sampleProduct3); // MacBook Pro
            entityManager.flush();

            // Act
            List<Product> productsWithPhone = productRepository.findByNameContainingIgnoreCase("phone");

            // Assert
            assertThat(productsWithPhone).hasSize(1);
            assertThat(productsWithPhone.get(0).getName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("Should find products by price range")
        void shouldFindProductsByPriceRange() {
            // Arrange
            entityManager.persist(sampleProduct1); // $999.99
            entityManager.persist(sampleProduct2); // $899.99
            entityManager.persist(sampleProduct3); // $2499.99
            entityManager.flush();

            // Act
            List<Product> productsInRange = productRepository.findByPriceBetween(
                    new BigDecimal("800.00"), new BigDecimal("1000.00"));

            // Assert
            assertThat(productsInRange).hasSize(2);
            assertThat(productsInRange).extracting(Product::getName)
                    .containsExactlyInAnyOrder("iPhone 15", "Samsung Galaxy S24");
        }

        @Test
        @DisplayName("Should find affordable products under max price")
        void shouldFindAffordableProductsUnderMaxPrice() {
            // Arrange
            entityManager.persist(sampleProduct1); // $999.99
            entityManager.persist(sampleProduct2); // $899.99
            entityManager.persist(sampleProduct3); // $2499.99
            entityManager.flush();

            // Act
            List<Product> affordableProducts = productRepository.findAffordableProducts(new BigDecimal("1000.00"));

            // Assert
            assertThat(affordableProducts).hasSize(2);
            assertThat(affordableProducts).extracting(Product::getName)
                    .containsExactlyInAnyOrder("iPhone 15", "Samsung Galaxy S24");
        }

        @Test
        @DisplayName("Should find products with high stock")
        void shouldFindProductsWithHighStock() {
            // Arrange
            entityManager.persist(sampleProduct1); // quantity 50
            entityManager.persist(sampleProduct2); // quantity 30
            entityManager.persist(sampleProduct3); // quantity 15
            entityManager.flush();

            // Act
            List<Product> highStockProducts = productRepository.findByQuantityGreaterThan(20);

            // Assert
            assertThat(highStockProducts).hasSize(2);
            assertThat(highStockProducts).extracting(Product::getName)
                    .containsExactlyInAnyOrder("iPhone 15", "Samsung Galaxy S24");
        }

        @Test
        @DisplayName("Should find products in stock")
        void shouldFindProductsInStock() {
            // Arrange
            // Create a product with zero quantity
            Product outOfStockProduct = new Product();
            outOfStockProduct.setName("Out of Stock Product");
            outOfStockProduct.setDescription("No inventory");
            outOfStockProduct.setPrice(new BigDecimal("99.99"));
            outOfStockProduct.setQuantity(0);

            entityManager.persist(sampleProduct1); // quantity 50
            entityManager.persist(sampleProduct2); // quantity 30
            entityManager.persist(outOfStockProduct); // quantity 0
            entityManager.flush();

            // Act
            List<Product> inStockProducts = productRepository.findInStockProducts();

            // Assert
            assertThat(inStockProducts).hasSize(2);
            assertThat(inStockProducts).extracting(Product::getName)
                    .containsExactlyInAnyOrder("iPhone 15", "Samsung Galaxy S24");
        }
    }

    @Nested
    @DisplayName("Data Validation Tests")
    class DataValidationTests {

        @Test
        @DisplayName("Should handle null name search gracefully")
        void shouldHandleNullNameSearchGracefully() {
            // Act
            List<Product> foundProducts = productRepository.findByNameContainingIgnoreCase(null);

            // Assert
            assertThat(foundProducts).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty name search gracefully")
        void shouldHandleEmptyNameSearchGracefully() {
            // Arrange
            entityManager.persistAndFlush(sampleProduct1);

            // Act
            List<Product> foundProducts = productRepository.findByNameContainingIgnoreCase("");

            // Assert
            assertThat(foundProducts).hasSize(1);
        }

        @Test
        @DisplayName("Should handle case insensitive name search")
        void shouldHandleCaseInsensitiveNameSearch() {
            // Arrange
            entityManager.persistAndFlush(sampleProduct1); // "iPhone 15"

            // Act
            List<Product> foundProducts = productRepository.findByNameContainingIgnoreCase("iphone");

            // Assert
            assertThat(foundProducts).hasSize(1);
            assertThat(foundProducts.get(0).getName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("Should handle price range queries properly")
        void shouldHandlePriceRangeQueriesProperly() {
            // Arrange
            entityManager.persist(sampleProduct1);
            entityManager.persist(sampleProduct2);
            entityManager.persist(sampleProduct3);
            entityManager.flush();

            // Act
            List<Product> expensiveProducts = productRepository.findByPriceBetween(
                    new BigDecimal("2000.00"), new BigDecimal("3000.00"));

            // Assert
            assertThat(expensiveProducts).hasSize(1);
            assertThat(expensiveProducts.get(0).getName()).isEqualTo("MacBook Pro");
        }

        @Test
        @DisplayName("Should handle quantity comparison properly")
        void shouldHandleQuantityComparisonProperly() {
            // Arrange
            entityManager.persist(sampleProduct1);
            entityManager.persist(sampleProduct2);
            entityManager.persist(sampleProduct3);
            entityManager.flush();

            // Act
            List<Product> limitedQuantityProducts = productRepository.findByQuantityGreaterThan(40);

            // Assert
            assertThat(limitedQuantityProducts).hasSize(1);
            assertThat(limitedQuantityProducts.get(0).getName()).isEqualTo("iPhone 15");
        }
    }
}
