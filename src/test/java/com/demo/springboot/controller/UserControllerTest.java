package com.demo.springboot.controller;

import com.demo.springboot.entity.User;
import com.demo.springboot.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_ShouldReturnPagedUsers() throws Exception {
        User user1 = new User("John Doe", "john@example.com", "1234567890");
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", "0987654321");
        user2.setId(2L);
        
        Page<User> userPage = new PageImpl<>(Arrays.asList(user1, user2), PageRequest.of(0, 10), 2);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].name").value("Jane Smith"));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        User user = new User("John Doe", "john@example.com", "1234567890");
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() throws Exception {
        User inputUser = new User("John Doe", "john@example.com", "1234567890");
        User savedUser = new User("John Doe", "john@example.com", "1234567890");
        savedUser.setId(1L);
        
        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
        User updatedUser = new User("John Updated", "john.updated@example.com", "1234567890");
        updatedUser.setId(1L);
        
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
