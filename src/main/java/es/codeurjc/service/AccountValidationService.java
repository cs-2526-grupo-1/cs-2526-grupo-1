package es.codeurjc.service;

import org.springframework.stereotype.Service;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
public class AccountValidationService {

    public void validateAccountDeletion(Account account) {
        if (BigDecimal.valueOf(account.getBalance()).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Cannot delete account with non-zero balance");
        }
    }

    public void validateAmount(double amount, double max, String errorMsg) {
        BigDecimal bdAmount = BigDecimal.valueOf(amount);

        if (bdAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (bdAmount.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public void checkSufficientFunds(double amount, double balance) {
        if (BigDecimal.valueOf(balance).compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }

    public void validateUserAgeForTransfer(User user) {
        if (user.getBirthdate() != null &&
                Period.between(user.getBirthdate(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("User must be 18 years old to make transfers");
        }
    }
}
