package es.codeurjc.service.credit_scoring;

/**
 * Interface for external credit scoring service.
 */
public interface CreditScoringService {
    
    /**
     * Get credit score for a customer
     * @param dni customer DNI
     * @return credit score (300-850)
     */
    int getCreditScore(String dni);
}
