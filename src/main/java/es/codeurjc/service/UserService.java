package es.codeurjc.service;

import es.codeurjc.model.User;
import es.codeurjc.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing users.
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
    
    /**
     * Get user by DNI
     */
    public User getUserByDni(String dni) {
        return userRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("User not found with DNI: " + dni));
    }
    
    /**
     * Save or update user
     */
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    /**
     * Check if DNI already exists
     */
    public boolean isDniRegistered(String dni) {
        return userRepository.existsByDni(dni);
    }
    
    /**
     * Check if email already exists
     */
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }
}
