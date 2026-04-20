package es.codeurjc.unit;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.codeurjc.model.Account;
import es.codeurjc.model.Transaction;
import es.codeurjc.model.User;
import es.codeurjc.model.Transaction;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.AccountService;
import es.codeurjc.service.RandomService;
import es.codeurjc.model.Notification;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("When running AccountService ")
class AccountServiceTest {

        @Mock
        private AccountRepository accountRepository;
        @Mock
        private TransactionRepository transactionRepository;
        @Mock
        private EmailNotificationService emailService;
        @Mock
        private SmsNotificationService smsService;
        @Mock
        private RandomService randomService;

        @InjectMocks
        private AccountService accountService;

        private User emailUser;
        private User smsUser;
        private Account accountA;
        private Account accountB;

        private static final String ACC_A = "ES0000000001";
        private static final String ACC_B = "ES0000000002";
        private static final String ACC_MISSING = "ES9999999999";

        @BeforeEach
        void setUp() {
                emailUser = new User("user1", "pass", "ROLE_USER");
                emailUser.setEmail("user1@test.com");
                emailUser.setNotificationType(User.NotificationType.EMAIL);

                smsUser = new User("user2", "pass", "ROLE_USER");
                smsUser.setPhone("600000000");
                smsUser.setNotificationType(User.NotificationType.SMS);

                accountA = new Account(ACC_A, Account.AccountType.CHECKING, 500.0);
                accountA.setUser(emailUser);

                accountB = new Account(ACC_B, Account.AccountType.CHECKING, 200.0);
                accountB.setUser(smsUser);
        }

        @Test
        @DisplayName("withdraw zero or negative amount should throw IllegalArgumentException")
        public void withdrawZeroOrNegativeAmountShouldThrowIllegalArgumentException() {
                assertThatThrownBy(() -> accountService.withdraw(ACC_A, -200, "Withdraw negative amount")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("withdraw an amount that exceeds limit should throw IllegalArgumentException")
        public void withdrawExceedLimitAmountShouldThrowIllegalArgumentException() {
                assertThatThrownBy(() -> accountService.withdraw(ACC_A, 6000, "Withdraw a lot of money")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Amount exceeds maximum withdrawal limit");
        }

        @Test
        @DisplayName("withdraw an amount that exceeds balance should throw IllegalArgumentException")
        public void withdrawAmountThatExceedsBalanceShouldThrowIllegalArgumentException() {
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));
                assertThatThrownBy(() -> accountService.withdraw(ACC_B, 300, "Padel match was a bit expensive")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("withdraw a valid amount in an account with email notification triggers an email notification")
        public void withdrawValidAmountDecreasesBalanceAndTriggersEmailNotification(){
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));
                when(accountRepository.save(accountA)).thenReturn(accountA);

                double balanceBefore = accountA.getBalance();
                accountService.withdraw(ACC_A, 100, "Padel match");
                assertThat(accountA.getBalance()).isEqualTo(balanceBefore - 100);
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountA);
                verify(emailService).sendNotification(emailUser, Notification.NotificationType.WITHDRAWAL,
                                "Withdrawal Confirmation",
                                String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", 100.0,
                                                accountA.getBalance()));
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("withdraw a valid amount in an account with SMS notification triggers an SMS notification")
        public void withdrawValidAmountDecreasesBalanceAndTriggersSmsNotification(){
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));
                when(accountRepository.save(accountB)).thenReturn(accountB);

                double balanceBefore = accountB.getBalance();
                accountService.withdraw(ACC_B, 100, "Padel match");

                assertThat(accountB.getBalance()).isEqualTo(balanceBefore - 100);
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountB);
                verify(smsService).sendNotification(smsUser, Notification.NotificationType.WITHDRAWAL, "Withdrawal",
                                String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", 100.0,
                                                accountB.getBalance()));
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("create account")
        void testCreateAccount() {
                // Given
                User user = this.emailUser;
                Account testAccount = new Account("1", Account.AccountType.CHECKING, 0);
                testAccount.setUser(user);

                when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

                // When
                Account result = accountService.createAccount(user, Account.AccountType.CHECKING);

                // Then
                verify(accountRepository).save(any(Account.class));
                assertThat(result).isNotNull();
                assertThat(result.getUser()).isEqualTo(user);
                assertThat(result.getAccountType()).isEqualTo(Account.AccountType.CHECKING);
                assertThat(result.getBalance()).isEqualTo(0);
                assertThat(result.getAccountNumber()).isEqualTo("1");
                assertThat(result).isEqualTo(testAccount);
        }

        @Test
        @DisplayName("deposit zero or negative amount should throw IllegalArgumentException")
        public void depositZeroOrNegativeAmountShouldThrowIllegalArgumentException() {
                assertThatThrownBy(() -> accountService.deposit(ACC_A, -200, "Deposit negative amount"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("deposit an amount that exceeds limit should throw IllegalArgumentException")
        public void depositExceedLimitAmountShouldThrowIllegalArgumentException() {
                assertThatThrownBy(() -> accountService.deposit(ACC_A, 10001, "deposit a lot of money"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Amount exceeds maximum deposit limit");
        }

        @Test
        @DisplayName("deposit a valid amount in an account with email notification with description")
        void testDepositWithDescriptionEmail() {
                // Given
                mockAccountFound(ACC_A, accountA);

                // When
                Account result = accountService.deposit(ACC_A, 100.0, "Transaction Test");

                // Then
                assertThat(result.getBalance()).isEqualTo(600.0);
                checkCommonDepositVerifications();
                verify(emailService).sendNotification(eq(emailUser), any(), any(), contains("100.00"));
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deposit a valid amount in an account with SMS notification with description")
        void testDepositWithDescriptionSms() {
                // Given
                mockAccountFound(ACC_B, accountB);

                // When
                Account result = accountService.deposit(ACC_B, 50.0, "Transaction Test");

                // Then
                assertThat(result.getBalance()).isEqualTo(250.0);
                checkCommonDepositVerifications();
                verify(smsService).sendNotification(eq(smsUser), any(), any(), contains("50.00"));
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deposit a valid amount in an account with Email notification without description")
        void testNoDescriptionDepositSuccessEmail() {
                // Given
                mockAccountFound(ACC_A, accountA);

                // When
                Account result = accountService.deposit(ACC_A, 100.0);

                // Then
                assertThat(result.getBalance()).isEqualTo(600.0);
                checkCommonDepositVerifications();
                verify(emailService).sendNotification(eq(emailUser), any(), any(), contains("100.00"));
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deposit a valid amount in an account with SMS notification without description")
        void testNoDescriptionDepositSuccessSms() {
                // Given
                mockAccountFound(ACC_B, accountB);

                // When
                Account result = accountService.deposit(ACC_B, 50.0);

                // Then
                assertThat(result.getBalance()).isEqualTo(250.0);
                checkCommonDepositVerifications();
                verify(smsService).sendNotification(eq(smsUser), any(), any(), contains("50.00"));
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        void testTransactionEntityCreation() {
                // Given
                mockAccountFound(ACC_A, accountA);

                // When
                accountService.deposit(ACC_A, 200.0, "Test");

                // Then
                checkCommonDepositVerifications();
        }

        private void mockAccountFound(String accNumber, Account account) {
                when(accountRepository.findByAccountNumber(accNumber)).thenReturn(Optional.of(account));
                when(accountRepository.save(any(Account.class))).thenReturn(account);
        }

        private void checkCommonDepositVerifications() {
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(any(Account.class));
        }

        @DisplayName("withdraw with user without notification type should not send notifications")
        public void withdrawWithUserWithoutNotificationTypeShouldNotSendNotifications() {
                User noNotifUser = new User("user3", "pass", "ROLE_USER");
                noNotifUser.setNotificationType(null);
                String ACC_C = "ES0000000003";
                double originalBalance = 500.0;
                Account accountC = new Account(ACC_C, Account.AccountType.CHECKING, originalBalance);
                accountC.setUser(noNotifUser);

                when(accountRepository.findByAccountNumber(ACC_C)).thenReturn(Optional.of(accountC));
                when(accountRepository.save(accountC)).thenReturn(accountC);

                accountService.withdraw(ACC_C, 100, "Test");

                assertThat(accountC.getBalance()).isEqualTo(originalBalance - 100);
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountC);
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("withdraw from non-existent account should throw IllegalArgumentException")
        public void withdrawFromNonExistentAccountShouldThrowException() {
                when(accountRepository.findByAccountNumber(ACC_MISSING)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> accountService.withdraw(ACC_MISSING, 100, "Test"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("getUserAccounts returns the accounts associated to a user")
        public void getUserAccountsReturnsUserAccounts() {
                when(accountRepository.findByUser(emailUser)).thenReturn(List.of(accountA, accountB));
                assertThat(accountService.getUserAccounts(emailUser)).isEqualTo(List.of(accountA, accountB));
        }
        
}