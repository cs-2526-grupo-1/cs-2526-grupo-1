package es.codeurjc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final RoleBasedAuthenticationSuccessHandler successHandler;

    public WebSecurityConfig(RoleBasedAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll() // Allow Chrome DevTools endpoints
                        .requestMatchers("/dashboard/**", "/transfer/**").hasRole("CUSTOMER")
                        .requestMatchers("/loan/request/**").hasRole("CUSTOMER")
                        .requestMatchers("/loan/manage/**").hasRole("MANAGER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(successHandler))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}
