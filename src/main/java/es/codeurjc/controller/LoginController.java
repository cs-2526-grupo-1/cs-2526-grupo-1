package es.codeurjc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for login functionality.
 * Authentication is handled by Spring Security.
 */
@Controller
public class LoginController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String showLoginPage(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

         // Get application version from JAR manifest
        String version = getApplicationVersion();
        model.addAttribute("version", version);

        return "login";
    }

    /**
     * Get application version from JAR manifest.
     * Returns "DEV" if version cannot be determined.
     */
    private String getApplicationVersion() {
        Package pkg = LoginController.class.getPackage();
        
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        
        return "DEV";
    }
}
