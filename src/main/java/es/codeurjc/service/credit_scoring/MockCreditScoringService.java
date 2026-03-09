package es.codeurjc.service.credit_scoring;

import org.springframework.stereotype.Service;

/**
 * Mock implementation of CreditScoringService for development.
 * Returns a fixed credit score for demo purposes.
 */
@Service
public class MockCreditScoringService implements CreditScoringService {
    
    @Override
    public int getCreditScore(String dni) {
        // Return a fixed score for demo purposes
        // In production, this would call an external API
        return 650;
    }
}
