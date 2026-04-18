package es.codeurjc.unit;

import es.codeurjc.model.Account;
import es.codeurjc.model.Transaction;
import es.codeurjc.model.User;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.AccountService;
import es.codeurjc.service.RandomService;
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
        public void withrawZeroOrNegativeAmountShouldThrowIllegalArgumentException() {
                assertThatThrownBy(() -> accountService.withdraw(ACC_A, -200, "Withdraw negative amount")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("withdraw an amount that exceeds limit should throw IllegalArgumentException")
        public void withrawExceedLimitAmountShouldThrowIllegalArgumentException() {
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
                accountService.withdraw(ACC_A, 100, "Padel match");
                assertThat(accountA.getBalance()).isEqualTo(400);
                verify(emailService).sendNotification(any(), any(), any(), any());
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("withdraw a valid amount in an account with SMS notification triggers an SMS notification")
        public void withdrawValidAmountDecreasesBalanceAndTriggersSmsNotification(){
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));
                when(accountRepository.save(accountB)).thenReturn(accountB);
                accountService.withdraw(ACC_B, 100, "Padel match");
                assertThat(accountB.getBalance()).isEqualTo(100);
                verify(smsService).sendNotification(any(), any(), any(), any());
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        
}
