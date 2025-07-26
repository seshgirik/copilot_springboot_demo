package com.demo.springboot.repository;

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

import com.demo.springboot.entity.User;

/**
 * Unit tests for UserRepository
 * Tests JPA repository methods and custom queries
 */
@DataJpaTest
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User sampleUser1;
    private User sampleUser2;
    private User sampleUser3;

    @BeforeEach
    void setUp() {
        sampleUser1 = new User();
        sampleUser1.setName("John Doe");
        sampleUser1.setEmail("john.doe@example.com");
        sampleUser1.setPhone("123-456-7890");

        sampleUser2 = new User();
        sampleUser2.setName("Jane Smith");
        sampleUser2.setEmail("jane.smith@example.com");
        sampleUser2.setPhone("987-654-3210");

        sampleUser3 = new User();
        sampleUser3.setName("Bob Johnson");
        sampleUser3.setEmail("bob.johnson@example.com");
        sampleUser3.setPhone("555-123-4567");
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save user successfully")
        void shouldSaveUserSuccessfully() {
            // Act
            User savedUser = userRepository.save(sampleUser1);

            // Assert
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getName()).isEqualTo("John Doe");
            assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(savedUser.getPhone()).isEqualTo("123-456-7890");
        }

        @Test
        @DisplayName("Should find user by ID successfully")
        void shouldFindUserByIdSuccessfully() {
            // Arrange
            User persistedUser = entityManager.persistAndFlush(sampleUser1);
            Long userId = persistedUser.getId();

            // Act
            Optional<User> foundUser = userRepository.findById(userId);

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getName()).isEqualTo("John Doe");
            assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
            assertThat(foundUser.get().getPhone()).isEqualTo("123-456-7890");
        }

        @Test
        @DisplayName("Should return empty when user not found by ID")
        void shouldReturnEmptyWhenUserNotFoundById() {
            // Act
            Optional<User> foundUser = userRepository.findById(999L);

            // Assert
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should find all users successfully")
        void shouldFindAllUsersSuccessfully() {
            // Arrange
            entityManager.persist(sampleUser1);
            entityManager.persist(sampleUser2);
            entityManager.persist(sampleUser3);
            entityManager.flush();

            // Act
            List<User> users = userRepository.findAll();

            // Assert
            assertThat(users).hasSize(3);
            assertThat(users).extracting(User::getName)
                    .containsExactlyInAnyOrder("John Doe", "Jane Smith", "Bob Johnson");
        }

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // Arrange
            User persistedUser = entityManager.persistAndFlush(sampleUser1);
            Long userId = persistedUser.getId();

            // Act
            userRepository.deleteById(userId);
            entityManager.flush();

            // Assert
            Optional<User> deletedUser = userRepository.findById(userId);
            assertThat(deletedUser).isEmpty();
        }

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Arrange
            User persistedUser = entityManager.persistAndFlush(sampleUser1);
            Long userId = persistedUser.getId();

            // Act
            persistedUser.setName("Updated Name");
            persistedUser.setEmail("updated@example.com");
            persistedUser.setPhone("999-888-7777");
            User updatedUser = userRepository.save(persistedUser);
            entityManager.flush();

            // Assert
            assertThat(updatedUser.getId()).isEqualTo(userId);
            assertThat(updatedUser.getName()).isEqualTo("Updated Name");
            assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
            assertThat(updatedUser.getPhone()).isEqualTo("999-888-7777");
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should find users with pagination")
        void shouldFindUsersWithPagination() {
            // Arrange
            entityManager.persist(sampleUser1);
            entityManager.persist(sampleUser2);
            entityManager.persist(sampleUser3);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 2);

            // Act
            Page<User> userPage = userRepository.findAll(pageable);

            // Assert
            assertThat(userPage.getContent()).hasSize(2);
            assertThat(userPage.getTotalElements()).isEqualTo(3);
            assertThat(userPage.getTotalPages()).isEqualTo(2);
            assertThat(userPage.isFirst()).isTrue();
            assertThat(userPage.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should find second page of users")
        void shouldFindSecondPageOfUsers() {
            // Arrange
            entityManager.persist(sampleUser1);
            entityManager.persist(sampleUser2);
            entityManager.persist(sampleUser3);
            entityManager.flush();

            Pageable pageable = PageRequest.of(1, 2);

            // Act
            Page<User> userPage = userRepository.findAll(pageable);

            // Assert
            assertThat(userPage.getContent()).hasSize(1);
            assertThat(userPage.getTotalElements()).isEqualTo(3);
            assertThat(userPage.getTotalPages()).isEqualTo(2);
            assertThat(userPage.isLast()).isTrue();
            assertThat(userPage.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsersExist() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<User> userPage = userRepository.findAll(pageable);

            // Assert
            assertThat(userPage.getContent()).isEmpty();
            assertThat(userPage.getTotalElements()).isEqualTo(0);
            assertThat(userPage.getTotalPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Arrange
            entityManager.persistAndFlush(sampleUser1);

            // Act
            Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getName()).isEqualTo("John Doe");
            assertThat(foundUser.get().getPhone()).isEqualTo("123-456-7890");
        }

        @Test
        @DisplayName("Should return empty when user not found by email")
        void shouldReturnEmptyWhenUserNotFoundByEmail() {
            // Act
            Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

            // Assert
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should check if user exists by email")
        void shouldCheckIfUserExistsByEmail() {
            // Arrange
            entityManager.persistAndFlush(sampleUser1);

            // Act & Assert
            assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should find users by name containing")
        void shouldFindUsersByNameContaining() {
            // Arrange
            entityManager.persist(sampleUser1); // John Doe
            entityManager.persist(sampleUser2); // Jane Smith
            entityManager.persist(sampleUser3); // Bob Johnson
            entityManager.flush();

            // Act
            List<User> usersWithJo = userRepository.findByNameContainingIgnoreCase("Jo");

            // Assert
            assertThat(usersWithJo).hasSize(2);
            assertThat(usersWithJo).extracting(User::getName)
                    .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
        }

        @Test
        @DisplayName("Should find users by phone containing")
        void shouldFindUsersByPhoneContaining() {
            // Arrange
            entityManager.persist(sampleUser1); // 123-456-7890
            entityManager.persist(sampleUser2); // 987-654-3210
            entityManager.persist(sampleUser3); // 555-123-4567
            entityManager.flush();

            // Act
            List<User> usersWithPhone123 = userRepository.findByPhoneContaining("123");

            // Assert
            assertThat(usersWithPhone123).hasSize(2);
            assertThat(usersWithPhone123).extracting(User::getName)
                    .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
        }

        @Test
        @DisplayName("Should find users by phone area code")
        void shouldFindUsersByPhoneAreaCode() {
            // Arrange
            entityManager.persist(sampleUser1); // 123-456-7890
            entityManager.persist(sampleUser2); // 987-654-3210
            entityManager.persist(sampleUser3); // 555-123-4567
            entityManager.flush();

            // Act
            List<User> usersWithAreaCode555 = userRepository.findByPhoneContaining("555");

            // Assert
            assertThat(usersWithAreaCode555).hasSize(1);
            assertThat(usersWithAreaCode555.get(0).getName()).isEqualTo("Bob Johnson");
            assertThat(usersWithAreaCode555.get(0).getPhone()).isEqualTo("555-123-4567");
        }
    }

    @Nested
    @DisplayName("Data Validation Tests")
    class DataValidationTests {

        @Test
        @DisplayName("Should handle null email gracefully")
        void shouldHandleNullEmailGracefully() {
            // Arrange
            User userWithNullEmail = new User();
            userWithNullEmail.setName("Test User");
            userWithNullEmail.setEmail(null);
            userWithNullEmail.setPhone("123-456-7890");

            // Act & Assert - This should not throw an exception for finding
            Optional<User> foundUser = userRepository.findByEmail(null);
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty email gracefully")
        void shouldHandleEmptyEmailGracefully() {
            // Act
            Optional<User> foundUser = userRepository.findByEmail("");

            // Assert
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should handle case insensitive name search")
        void shouldHandleCaseInsensitiveNameSearch() {
            // Arrange
            entityManager.persistAndFlush(sampleUser1); // "John Doe"

            // Act
            List<User> foundUsers = userRepository.findByNameContainingIgnoreCase("john");

            // Assert
            assertThat(foundUsers).hasSize(1);
            assertThat(foundUsers.get(0).getName()).isEqualTo("John Doe");
        }
    }
}
