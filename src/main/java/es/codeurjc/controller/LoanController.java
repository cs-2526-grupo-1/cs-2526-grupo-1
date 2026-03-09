package es.codeurjc.controller;

import es.codeurjc.model.User;
import es.codeurjc.model.Loan;
import es.codeurjc.service.UserService;
import es.codeurjc.service.loan.LoanService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for loan operations.
 */
@Controller
@RequestMapping("/loan")
public class LoanController {

    private final UserService userService;
    private final LoanService loanService;

    public LoanController(UserService userService,
            LoanService loanService) {
        this.userService = userService;
        this.loanService = loanService;
    }

    @GetMapping("/request")
    public String showLoanRequestForm(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            return "loan-request";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/request")
    public String requestLoan(Authentication authentication,
            @RequestParam double amount,
            @RequestParam int termMonths,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            loanService.requestLoan(user, amount, termMonths);
            redirectAttributes.addFlashAttribute("success",
                    "Loan request submitted successfully");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loan/request";
        }
    }

    @GetMapping("/manage")
    public String showLoanManagement(Model model) {
        try {
            List<Loan> pendingLoans = loanService.getPendingLoans();
            model.addAttribute("loans", pendingLoans);
            return "loan-manage";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/details")
    public String showLoanDetails(@RequestParam Long id, Model model) {
        try {
            Loan loan = loanService.getLoan(id);
            model.addAttribute("loan", loan);
            return "loan-details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/approve")
    public String approveLoan(@RequestParam Long loanId,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.approveLoan(loanId);
            redirectAttributes.addFlashAttribute("success", "Loan approved");
            return "redirect:/loan/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loan/manage";
        }
    }

    @PostMapping("/reject")
    public String rejectLoan(@RequestParam Long loanId,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.rejectLoan(loanId, reason);
            redirectAttributes.addFlashAttribute("success", "Loan rejected");
            return "redirect:/loan/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loan/manage";
        }
    }
}
