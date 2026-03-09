package es.codeurjc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.codeurjc.model.User;
import es.codeurjc.service.UserService;

@ControllerAdvice(basePackages = "es.codeurjc.controller")
public class UserModelAttributes {

    @Autowired
    private UserService userService;

    @ModelAttribute("fullname")
    public String getFullName() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "";
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user;

        try {
            user = userService.getUserByUsername(username);
        } catch (Exception e) {
            return "";
        }
        return user.getFullName();
    }

}
