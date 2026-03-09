package es.codeurjc.service.loan;

import es.codeurjc.model.LoanEvaluationResult;

public class LoanApprovalAlgorithm {

    public LoanEvaluationResult evaluate(LoanRequest request) {
        return new LoanEvaluationResult(true, "Aprobado");
    }
}
