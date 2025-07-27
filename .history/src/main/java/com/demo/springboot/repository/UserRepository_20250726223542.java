package com.demo.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.demo.springboot.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT u FROM User u WHERE u.phone LIKE %:phone%")
    List<User> findByPhoneContaining(@Param("phone") String phone);
    
    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = ?2, u.accountNonLocked = ?3, u.lockTime = ?4 WHERE u.email = ?1")
    void updateLockoutFieldsByEmail(String email, int failedLoginAttempts, boolean accountNonLocked, java.time.LocalDateTime lockTime);
}
