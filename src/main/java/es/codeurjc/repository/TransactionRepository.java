package es.codeurjc.repository;

import es.codeurjc.model.Account;
import es.codeurjc.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

/**
 * Repository for Transaction entity.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByAccount(Account account);
    
    List<Transaction> findByAccountOrderByTimestampDesc(Account account);

    List<Transaction> findByAccountAndTypeAndTimestampAfter(
            Account account, 
            Transaction.TransactionType type, 
            LocalDateTime timestamp
    );
}
