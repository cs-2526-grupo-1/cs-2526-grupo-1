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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.codeurjc.model.Account;
import es.codeurjc.model.Transaction;
import es.codeurjc.model.User;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.AccountService;
import es.codeurjc.service.RandomService;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;

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
                assertThatThrownBy(() -> accountService.withdraw(ACC_A, -200, "Withdraw negative amount"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("withdraw an amount that exceeds limit should throw IllegalArgumentException")
        public void withdrawExceedLimitAmountShouldThrowIllegalArgumentException() {
                assertThatThrownBy(() -> accountService.withdraw(ACC_A, 6000, "Withdraw a lot of money"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Amount exceeds maximum withdrawal limit");
        }

        @Test
        @DisplayName("withdraw an amount that exceeds balance should throw IllegalArgumentException")
        public void withdrawAmountThatExceedsBalanceShouldThrowIllegalArgumentException() {
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));
                assertThatThrownBy(() -> accountService.withdraw(ACC_B, 300, "Padel match was a bit expensive"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("withdraw a valid amount in an account with email notification triggers an email notification")
        public void withdrawValidAmountDecreasesBalanceAndTriggersEmailNotification() {
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));
                when(accountRepository.save(accountA)).thenReturn(accountA);

                double balanceBefore = accountA.getBalance();
                accountService.withdraw(ACC_A, 100, "Padel match");

                assertThat(accountA.getBalance()).isEqualTo(balanceBefore - 100);
                verify(emailService).sendNotification(any(), any(), any(), any());
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("withdraw a valid amount in an account with SMS notification triggers an SMS notification")
        public void withdrawValidAmountDecreasesBalanceAndTriggersSmsNotification() {
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));
                when(accountRepository.save(accountB)).thenReturn(accountB);

                double balanceBefore = accountB.getBalance();
                accountService.withdraw(ACC_B, 100, "Padel match");

                assertThat(accountB.getBalance()).isEqualTo(balanceBefore - 100);
                verify(smsService).sendNotification(any(), any(), any(), any());
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("getUserAccounts returns the accounts associated to a user")
        public void getUserAccountsReturnsUserAccounts() {
                when(accountRepository.findByUser(emailUser)).thenReturn(List.of(accountA));
                assertThat(accountService.getUserAccounts(emailUser)).isEqualTo(List.of(accountA));
        }

        @Test
        @DisplayName("getTransactions returns transactions ordered by timestamp descending")
        public void getTransactionsReturnsOrderedList() {
                Transaction deposit = new Transaction(accountA, Transaction.TransactionType.DEPOSIT, 100.0, "dep");
                Transaction withdrawal = new Transaction(accountA, Transaction.TransactionType.WITHDRAWAL, 50.0, "wd");

                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));
                when(transactionRepository.findByAccountOrderByTimestampDesc(accountA))
                                .thenReturn(List.of(withdrawal, deposit));

                List<Transaction> result = accountService.getTransactions(ACC_A);

                assertThat(result).hasSize(2);
                assertThat(result.get(0).getType()).isEqualTo(Transaction.TransactionType.WITHDRAWAL);
                assertThat(result.get(1).getType()).isEqualTo(Transaction.TransactionType.DEPOSIT);
                verify(transactionRepository).findByAccountOrderByTimestampDesc(accountA);
        }

        @Test
        @DisplayName("getTransactions returns empty list when account has no transactions")
        public void getTransactionsReturnsEmptyListWhenNoTransactions() {
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));
                when(transactionRepository.findByAccountOrderByTimestampDesc(accountA)).thenReturn(List.of());

                assertThat(accountService.getTransactions(ACC_A)).isEmpty();
        }

        @Test
        @DisplayName("getTransactions for an unknown account should throw IllegalArgumentException")
        public void getTransactionsUnknownAccountShouldThrowIllegalArgumentException() {
                when(accountRepository.findByAccountNumber(ACC_MISSING)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> accountService.getTransactions(ACC_MISSING))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Account not found");

                verifyNoInteractions(transactionRepository);
        }
}
