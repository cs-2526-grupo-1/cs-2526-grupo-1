package es.codeurjc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.model.LoanEvaluationResult;

@Repository
public interface LoanEvaluationResultRepository extends JpaRepository<LoanEvaluationResult, Long> {
}
