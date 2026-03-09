package es.codeurjc.controller;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import es.codeurjc.service.AccountService;
import es.codeurjc.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for transfer operations.
 */
@Controller
public class TransferController {
    
    private final UserService userService;
    private final AccountService accountService;
    
    public TransferController(UserService userService,
                             AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }
    
    @GetMapping("/transfer")
    public String showTransferForm(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            List<Account> accounts = accountService.getUserAccounts(user);
            
            model.addAttribute("user", user);
            model.addAttribute("accounts", accounts);
            
            return "transfer";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/transfer")
    public String performTransfer(@RequestParam String fromAccount,
                                  @RequestParam String toAccount,
                                  @RequestParam double amount,
                                  RedirectAttributes redirectAttributes) {
        try {
            accountService.transfer(fromAccount, toAccount, amount);
            redirectAttributes.addFlashAttribute("success", 
                "Transfer completed successfully");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/transfer";
        }
    }
}
