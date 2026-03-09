package es.codeurjc.controller;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import es.codeurjc.model.Loan;
import es.codeurjc.model.Transaction;
import es.codeurjc.service.AccountService;
import es.codeurjc.service.UserService;
import es.codeurjc.service.loan.LoanService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for dashboard.
 */
@Controller
public class DashboardController {
    
    private final UserService userService;
    private final AccountService accountService;
    private final LoanService loanService;
    
    public DashboardController(UserService userService,
                              AccountService accountService,
                              LoanService loanService) {
        this.userService = userService;
        this.accountService = accountService;
        this.loanService = loanService;
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            List<Account> accounts = accountService.getUserAccounts(user);
            List<Loan> loans = loanService.getUserLoans(user);
            
            model.addAttribute("user", user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("loans", loans);
            
            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/account/transactions")
    public String showTransactions(@RequestParam String accountNumber, 
                                   Authentication authentication,
                                   Model model) {
        try {
            Account account = accountService.getAccount(accountNumber);
            List<Transaction> transactions = accountService.getTransactions(accountNumber);
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            model.addAttribute("user", user);
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
            
            return "transactions";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
