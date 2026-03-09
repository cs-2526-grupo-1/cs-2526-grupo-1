package es.codeurjc.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity representing a bank user with authentication.
 */
@Entity
@Table(name = "users")
public class User {
    
    public enum NotificationType {
        EMAIL, SMS
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    private String password; // Will be encoded by BCrypt
    
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();
    
    // Personal information
    private String firstName;
    private String lastName;
    private String dni;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private double monthlyIncome;
    
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType = NotificationType.EMAIL;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Loan> loans = new ArrayList<>();
    
    // Constructors
    public User() {
    }
    
    public User(String username, String password, String... roles) {
        this.username = username;
        this.password = password;
        this.roles = List.of(roles);
    }
    
    public User(String firstName, String lastName, String dni, String email, 
                String phone, String username, String password, 
                LocalDate registrationDate, double monthlyIncome, String... roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.registrationDate = registrationDate;
        this.monthlyIncome = monthlyIncome;
        this.roles = List.of(roles);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getDni() {
        return dni;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public double getMonthlyIncome() {
        return monthlyIncome;
    }
    
    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public List<Account> getAccounts() {
        return accounts;
    }
    
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
    
    public List<Loan> getLoans() {
        return loans;
    }
    
    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public long getYearsWithBank() {
        if (registrationDate == null) return 0;
        return LocalDate.now().getYear() - registrationDate.getYear();
    }
    
    public boolean hasMultipleProducts() {
        return accounts != null && accounts.size() > 1;
    }
}
