package com.demo.springboot.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.demo.springboot.entity.Product;
import com.demo.springboot.repository.ProductRepository;

/**
 * Unit tests for ProductService
 * Tests all CRUD operations and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

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
        @DisplayName("Should return all products when products exist")
        void getAllProducts_WhenProductsExist_ShouldReturnAllProducts() {
            System.out.println("START: ProductServiceTest.GetAllProductsTests.getAllProducts_WhenProductsExist_ShouldReturnAllProducts");
            // Given
            List<Product> products = Arrays.asList(sampleProduct, sampleProduct2);
            when(productRepository.findAll()).thenReturn(products);

            // When
            List<Product> result = productService.getAllProducts();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(sampleProduct, sampleProduct2);
            verify(productRepository).findAll();
            System.out.println("END: ProductServiceTest.GetAllProductsTests.getAllProducts_WhenProductsExist_ShouldReturnAllProducts");
        }

        @Test
        @DisplayName("Should return empty list when no products exist")
        void getAllProducts_WhenNoProductsExist_ShouldReturnEmptyList() {
            // Given
            when(productRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<Product> result = productService.getAllProducts();

            // Then
            assertThat(result).isEmpty();
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Should return paginated products")
        void getAllProducts_WithPagination_ShouldReturnPagedProducts() {
            // Given
            Pageable pageable = PageRequest.of(0, 2);
            Page<Product> productPage = new PageImpl<>(Arrays.asList(sampleProduct, sampleProduct2), pageable, 2);
            when(productRepository.findAll(pageable)).thenReturn(productPage);

            // When
            Page<Product> result = productService.getAllProducts(pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            verify(productRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Get Product By ID Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when product exists")
        void getProductById_WhenProductExists_ShouldReturnProduct() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            // When
            Optional<Product> result = productService.getProductById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(sampleProduct);
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when product not found")
        void getProductById_WhenProductNotFound_ShouldReturnEmpty() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = productService.getProductById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(productRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Search Products Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Should return products matching name search")
        void searchProductsByName_WhenProductsMatch_ShouldReturnMatchingProducts() {
            // Given
            List<Product> matchingProducts = Arrays.asList(sampleProduct);
            when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(matchingProducts);

            // When
            List<Product> result = productService.searchProductsByName("Test");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("Test");
            verify(productRepository).findByNameContainingIgnoreCase("Test");
        }

        @Test
        @DisplayName("Should return empty list when no products match name search")
        void searchProductsByName_WhenNoProductsMatch_ShouldReturnEmptyList() {
            // Given
            when(productRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(Arrays.asList());

            // When
            List<Product> result = productService.searchProductsByName("NonExistent");

            // Then
            assertThat(result).isEmpty();
            verify(productRepository).findByNameContainingIgnoreCase("NonExistent");
        }

        @Test
        @DisplayName("Should return products in price range")
        void getProductsByPriceRange_WhenProductsInRange_ShouldReturnProducts() {
            // Given
            BigDecimal minPrice = new BigDecimal("20.00");
            BigDecimal maxPrice = new BigDecimal("40.00");
            List<Product> productsInRange = Arrays.asList(sampleProduct);
            when(productRepository.findByPriceBetween(minPrice, maxPrice)).thenReturn(productsInRange);

            // When
            List<Product> result = productService.getProductsByPriceRange(minPrice, maxPrice);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPrice()).isBetween(minPrice, maxPrice);
            verify(productRepository).findByPriceBetween(minPrice, maxPrice);
        }

        @Test
        @DisplayName("Should return products in stock")
        void getProductsInStock_WhenProductsInStock_ShouldReturnProducts() {
            // Given
            List<Product> inStockProducts = Arrays.asList(sampleProduct, sampleProduct2);
            when(productRepository.findInStockProducts()).thenReturn(inStockProducts);

            // When
            List<Product> result = productService.getProductsInStock();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(product -> product.getQuantity() > 0);
            verify(productRepository).findInStockProducts();
        }

        @Test
        @DisplayName("Should return affordable products")
        void getAffordableProducts_WhenAffordableProductsExist_ShouldReturnProducts() {
            // Given
            BigDecimal maxPrice = new BigDecimal("30.00");
            List<Product> affordableProducts = Arrays.asList(sampleProduct);
            when(productRepository.findAffordableProducts(maxPrice)).thenReturn(affordableProducts);

            // When
            List<Product> result = productService.getAffordableProducts(maxPrice);

            // Then
            assertThat(result).hasSize(1);
            verify(productRepository).findAffordableProducts(maxPrice);
        }
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_WithValidProduct_ShouldCreateProduct() {
            // Given
            Product newProduct = new Product();
            newProduct.setName("New Product");
            newProduct.setPrice(new BigDecimal("39.99"));
            newProduct.setQuantity(75);

            Product savedProduct = new Product();
            savedProduct.setId(3L);
            savedProduct.setName("New Product");
            savedProduct.setPrice(new BigDecimal("39.99"));
            savedProduct.setQuantity(75);

            when(productRepository.save(newProduct)).thenReturn(savedProduct);

            // When
            Product result = productService.createProduct(newProduct);

            // Then
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getName()).isEqualTo("New Product");
            verify(productRepository).save(newProduct);
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully when product exists")
        void updateProduct_WhenProductExists_ShouldUpdateProduct() {
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

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            // When
            Product result = productService.updateProduct(1L, updateDetails);

            // Then
            assertThat(result.getName()).isEqualTo("Updated Product");
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("35.99"));
            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found for update")
        void updateProduct_WhenProductNotFound_ShouldThrowException() {
            // Given
            Product updateDetails = new Product();
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(999L, updateDetails))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully when product exists")
        void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository).findById(1L);
            verify(productRepository).delete(sampleProduct);
        }

        @Test
        @DisplayName("Should throw exception when product not found for deletion")
        void deleteProduct_WhenProductNotFound_ShouldThrowException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).delete(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Update Stock Tests")
    class UpdateStockTests {

        @Test
        @DisplayName("Should update stock successfully when product exists")
        void updateStock_WhenProductExists_ShouldUpdateStock() {
            // Given
            Integer newQuantity = 150;
            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Test Product");
            updatedProduct.setQuantity(newQuantity);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            // When
            Product result = productService.updateStock(1L, newQuantity);

            // Then
            assertThat(result.getQuantity()).isEqualTo(newQuantity);
            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found for stock update")
        void updateStock_WhenProductNotFound_ShouldThrowException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateStock(999L, 100))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found with id: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Total Count Tests")
    class TotalCountTests {

        @Test
        @DisplayName("Should return correct total count")
        void getTotalCount_ShouldReturnCorrectCount() {
            // Given
            when(productRepository.count()).thenReturn(5L);

            // When
            long result = productService.getTotalCount();

            // Then
            assertThat(result).isEqualTo(5L);
            verify(productRepository).count();
        }

        @Test
        @DisplayName("Should return zero when no products exist")
        void getTotalCount_WhenNoProducts_ShouldReturnZero() {
            // Given
            when(productRepository.count()).thenReturn(0L);

            // When
            long result = productService.getTotalCount();

            // Then
            assertThat(result).isEqualTo(0L);
            verify(productRepository).count();
        }
    }
}
