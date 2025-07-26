package com.demo.springboot.service;

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

import com.demo.springboot.entity.User;
import com.demo.springboot.repository.UserRepository;

/**
 * Unit tests for UserService
 * Tests all CRUD operations and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private User sampleUser2;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john.doe@example.com");
        sampleUser.setPhone("123-456-7890");

        sampleUser2 = new User();
        sampleUser2.setId(2L);
        sampleUser2.setName("Jane Smith");
        sampleUser2.setEmail("jane.smith@example.com");
        sampleUser2.setPhone("098-765-4321");
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users when users exist")
        void getAllUsers_WhenUsersExist_ShouldReturnAllUsers() {
            // Given
            List<User> users = Arrays.asList(sampleUser, sampleUser2);
            when(userRepository.findAll()).thenReturn(users);

            // When
            List<User> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(sampleUser, sampleUser2);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsers_WhenNoUsersExist_ShouldReturnEmptyList() {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<User> result = userService.getAllUsers();

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return paginated users")
        void getAllUsers_WithPagination_ShouldReturnPagedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 2);
            Page<User> userPage = new PageImpl<>(Arrays.asList(sampleUser, sampleUser2), pageable, 2);
            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // When
            Page<User> result = userService.getAllUsers(pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            verify(userRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when user exists")
        void getUserById_WhenUserExists_ShouldReturnUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            // When
            Optional<User> result = userService.getUserById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(sampleUser);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void getUserById_WhenUserNotFound_ShouldReturnEmpty() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userService.getUserById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should return users matching name search")
        void searchUsersByName_WhenUsersMatch_ShouldReturnMatchingUsers() {
            // Given
            List<User> matchingUsers = Arrays.asList(sampleUser);
            when(userRepository.findByNameContainingIgnoreCase("John")).thenReturn(matchingUsers);

            // When
            List<User> result = userService.searchUsersByName("John");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("John");
            verify(userRepository).findByNameContainingIgnoreCase("John");
        }

        @Test
        @DisplayName("Should return empty list when no users match name search")
        void searchUsersByName_WhenNoUsersMatch_ShouldReturnEmptyList() {
            // Given
            when(userRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(Arrays.asList());

            // When
            List<User> result = userService.searchUsersByName("NonExistent");

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findByNameContainingIgnoreCase("NonExistent");
        }

        @Test
        @DisplayName("Should return users matching phone search")
        void searchUsersByPhone_WhenUsersMatch_ShouldReturnMatchingUsers() {
            // Given
            List<User> matchingUsers = Arrays.asList(sampleUser);
            when(userRepository.findByPhoneContaining("123")).thenReturn(matchingUsers);

            // When
            List<User> result = userService.searchUsersByPhone("123");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPhone()).contains("123");
            verify(userRepository).findByPhoneContaining("123");
        }

        @Test
        @DisplayName("Should find user by exact email")
        void getUserByEmail_WhenUserExists_ShouldReturnUser() {
            // Given
            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));

            // When
            Optional<User> result = userService.getUserByEmail("john.doe@example.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
            verify(userRepository).findByEmail("john.doe@example.com");
        }

        @Test
        @DisplayName("Should check if email exists")
        void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
            // Given
            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            // When
            boolean result = userService.existsByEmail("john.doe@example.com");

            // Then
            assertThat(result).isTrue();
            verify(userRepository).existsByEmail("john.doe@example.com");
        }

        @Test
        @DisplayName("Should check if email does not exist")
        void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
            // Given
            when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

            // When
            boolean result = userService.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(result).isFalse();
            verify(userRepository).existsByEmail("nonexistent@example.com");
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when email is unique")
        void createUser_WithUniqueEmail_ShouldCreateUser() {
            // Given
            User newUser = new User();
            newUser.setName("New User");
            newUser.setEmail("new.user@example.com");
            newUser.setPhone("555-0123");

            User savedUser = new User();
            savedUser.setId(3L);
            savedUser.setName("New User");
            savedUser.setEmail("new.user@example.com");
            savedUser.setPhone("555-0123");

            when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
            when(userRepository.save(newUser)).thenReturn(savedUser);

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getName()).isEqualTo("New User");
            assertThat(result.getEmail()).isEqualTo("new.user@example.com");
            verify(userRepository).save(newUser);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUser_WithDuplicateEmail_ShouldThrowException() {
            // Given
            User newUser = new User();
            newUser.setEmail("john.doe@example.com");
            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with email john.doe@example.com already exists");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully when user exists")
        void updateUser_WhenUserExists_ShouldUpdateUser() {
            // Given
            User updateDetails = new User();
            updateDetails.setName("Updated User");
            updateDetails.setEmail("updated.user@example.com");
            updateDetails.setPhone("555-9999");

            User updatedUser = new User();
            updatedUser.setId(1L);
            updatedUser.setName("Updated User");
            updatedUser.setEmail("updated.user@example.com");
            updatedUser.setPhone("555-9999");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            // When
            User result = userService.updateUser(1L, updateDetails);

            // Then
            assertThat(result.getName()).isEqualTo("Updated User");
            assertThat(result.getEmail()).isEqualTo("updated.user@example.com");
            verify(userRepository).findById(1L);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found for update")
        void updateUser_WhenUserNotFound_ShouldThrowException() {
            // Given
            User updateDetails = new User();
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(999L, updateDetails))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found with id: 999");

            verify(userRepository).findById(999L);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully when user exists")
        void deleteUser_WhenUserExists_ShouldDeleteUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            // When
            userService.deleteUser(1L);

            // Then
            verify(userRepository).findById(1L);
            verify(userRepository).delete(sampleUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found for deletion")
        void deleteUser_WhenUserNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found with id: 999");

            verify(userRepository).findById(999L);
            verify(userRepository, never()).delete(any(User.class));
        }
    }

    @Nested
    @DisplayName("Total Count Tests")
    class TotalCountTests {

        @Test
        @DisplayName("Should return correct total count")
        void getTotalCount_ShouldReturnCorrectCount() {
            // Given
            when(userRepository.count()).thenReturn(3L);

            // When
            long result = userService.getTotalCount();

            // Then
            assertThat(result).isEqualTo(3L);
            verify(userRepository).count();
        }

        @Test
        @DisplayName("Should return zero when no users exist")
        void getTotalCount_WhenNoUsers_ShouldReturnZero() {
            // Given
            when(userRepository.count()).thenReturn(0L);

            // When
            long result = userService.getTotalCount();

            // Then
            assertThat(result).isEqualTo(0L);
            verify(userRepository).count();
        }
    }
}
