package es.codeurjc.repository;

import es.codeurjc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByDni(String dni);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByDni(String dni);
    
    boolean existsByEmail(String email);
}
