package es.codeurjc.repository;

import es.codeurjc.model.User;
import es.codeurjc.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Loan entity.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUser(User user);
    
    List<Loan> findByStatus(Loan.LoanStatus status);
    
    List<Loan> findByUserAndStatus(User user, Loan.LoanStatus status);
}
