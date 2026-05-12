package com.example.movietheater.repository;

import com.example.movietheater.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.profile ORDER BY u.createdAt DESC")
    List<User> findAllWithProfile();
    @Query("SELECT u FROM User u WHERE u.role <> com.example.movietheater.eenum.Role.ADMIN")
    List<User> findAllNotAdmin();
}