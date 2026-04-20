package es.codeurjc.unit;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        public void withdrawValidAmountDecreasesBalanceAndTriggersSmsNotification() {
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

                // When
                when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
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
                double currentBalance= accountA.getBalance();
                mockAccountFound(ACC_A, accountA);

                // When
                Account result = accountService.deposit(ACC_A, 100.00, "Transaction Test");

                // Then
                assertThat(result.getBalance()).isEqualTo(100.00+currentBalance);
                checkCommonDepositVerifications();
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountA);
                verify(emailService).sendNotification(emailUser, Notification.NotificationType.DEPOSIT,
                                "Deposit Confirmation",
                                String.format("Deposit of %.2f EUR. New balance: %.2f EUR",
                                100.0, accountA.getBalance()));
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deposit a valid amount in an account with SMS notification with description")
        void testDepositWithDescriptionSms() {
                // Given
                double currentBalance = accountB.getBalance();
                mockAccountFound(ACC_B, accountB);

                // When
                Account result = accountService.deposit(ACC_B, 50.0, "Transaction Test");

                // Then
                assertThat(result.getBalance()).isEqualTo(50 + currentBalance);
                checkCommonDepositVerifications();
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountB);
                verify(smsService).sendNotification(smsUser, Notification.NotificationType.DEPOSIT, "Deposit Confirmation",
                                String.format("Deposit: %.2f EUR. Balance: %.2f EUR",
                                50.0, accountB.getBalance()));
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deposit a valid amount in an account with Email notification without description")
        void testNoDescriptionDepositSuccessEmail() {
                // Given
                double currentBalance = accountA.getBalance();
                mockAccountFound(ACC_A, accountA);

                // When
                Account result = accountService.deposit(ACC_A, 100.0);

                // Then
                assertThat(result.getBalance()).isEqualTo(currentBalance+100.0);
                checkCommonDepositVerifications();
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountA);
                verify(emailService).sendNotification(emailUser, Notification.NotificationType.DEPOSIT,
                                "Deposit Confirmation",
                                String.format("Deposit of %.2f EUR. New balance: %.2f EUR",
                                100.0, accountA.getBalance()));
                verify(smsService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("deposit a valid amount in an account with SMS notification without description")
        void testNoDescriptionDepositSuccessSms() {
                // Given
                double currentBalance = accountB.getBalance();
                mockAccountFound(ACC_B, accountB);

                // When
                Account result = accountService.deposit(ACC_B, 50.0);

                // Then
                assertThat(result.getBalance()).isEqualTo(currentBalance + 50.0);
                checkCommonDepositVerifications();
                verify(transactionRepository).save(any(Transaction.class));
                verify(accountRepository).save(accountB);
                verify(smsService).sendNotification(smsUser, Notification.NotificationType.DEPOSIT, "Deposit Confirmation",
                                String.format("Deposit: %.2f EUR. Balance: %.2f EUR",
                                50.0, accountB.getBalance()));
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
        @DisplayName("getUserAccounts returns empty list when user has no accounts")
        public void getUserAccountsReturnsEmptyListWhenNoAccounts() {
                when(accountRepository.findByUser(emailUser)).thenReturn(List.of());
                assertThat(accountService.getUserAccounts(emailUser)).isEmpty();
        }

        @Test
        @DisplayName("getUserAccounts returns the accounts associated to a user")
        public void getUserAccountsReturnsUserAccounts() {
                when(accountRepository.findByUser(emailUser)).thenReturn(List.of(accountA, accountB));
                assertThat(accountService.getUserAccounts(emailUser)).isEqualTo(List.of(accountA, accountB));
        }

        @Test
        @DisplayName("getBalance returns balance for an existing account")
        public void getBalanceExistingAccountReturnsBalance() {
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));

                assertThat(accountService.getBalance(ACC_A)).isEqualTo(500.0);
                verify(accountRepository).findByAccountNumber(ACC_A);
        }

        @Test
        @DisplayName("getBalance returns zero when balance is zero")
        public void getBalanceZeroBalanceReturnsZero() {
                Account empty = new Account("ES0000000010", Account.AccountType.SAVINGS, 0.0);
                empty.setUser(emailUser);
                when(accountRepository.findByAccountNumber("ES0000000010")).thenReturn(Optional.of(empty));

                assertThat(accountService.getBalance("ES0000000010")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getBalance for an unknown account should throw IllegalArgumentException")
        public void getBalanceUnknownAccountShouldThrowIllegalArgumentException() {
                when(accountRepository.findByAccountNumber(ACC_MISSING)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> accountService.getBalance(ACC_MISSING))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Account not found");
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

        private void givenValidTransferSetup() {
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));
                when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("transfer debits source account by transferred amount")
        public void transferSourceBalanceDecreasedByAmount() {
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                assertThat(accountA.getBalance()).isEqualTo(400.0);
        }

        @Test
        @DisplayName("transfer credits destination account by transferred amount")
        public void transferDestinationBalanceIncreasedByAmount() {
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                assertThat(accountB.getBalance()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("transfer saves exactly two transactions (SENT + RECEIVED)")
        public void transferSavesTwoTransactions() {
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("transfer sent transaction carries destination account number and amount")
        public void transferSentTransactionHasCorrectDestinationAndAmount() {
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
                verify(transactionRepository, times(2)).save(captor.capture());

                Transaction sent = captor.getAllValues().stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.TRANSFER_SENT)
                                .findFirst()
                                .orElseThrow();

                assertThat(sent.getDestinationAccountNumber()).isEqualTo(ACC_B);
                assertThat(sent.getAmount()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("transfer received transaction carries source account number and amount")
        public void transferReceivedTransactionHasCorrectSourceAndAmount() {
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
                verify(transactionRepository, times(2)).save(captor.capture());

                Transaction received = captor.getAllValues().stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.TRANSFER_RECEIVED)
                                .findFirst()
                                .orElseThrow();

                assertThat(received.getDestinationAccountNumber()).isEqualTo(ACC_A);
                assertThat(received.getAmount()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("transfer persists both accounts after transfer")
        public void transferPersistsBothAccounts() {
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                verify(accountRepository, times(2)).save(any(Account.class));
        }

        @Test
        @DisplayName("transfer sends email notification to sender when notification type is EMAIL")
        public void transferEmailNotificationSentToSender() {
                accountA.setUser(emailUser);
                accountB.setUser(emailUser);
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                verify(emailService, atLeastOnce()).sendNotification(eq(emailUser), any(), any(), any());
                verifyNoInteractions(smsService);
        }

        @Test
        @DisplayName("transfer sends email notification to both parties when both use EMAIL")
        public void transferEmailNotificationSentToSenderAndRecipient() {
                accountA.setUser(emailUser);
                accountB.setUser(emailUser);
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                verify(emailService, times(2)).sendNotification(eq(emailUser), any(), any(), any());
        }

        @Test
        @DisplayName("transfer sends SMS notification when notification type is SMS")
        public void transferSmsNotificationSentWhenSmsType() {
                accountA.setUser(smsUser);
                accountB.setUser(smsUser);
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                verify(smsService, atLeastOnce()).sendNotification(eq(smsUser), any(), any(), any());
                verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("transfer does not send SMS when notification type is EMAIL")
        public void transferNoSmsNotificationWhenEmailType() {
                accountA.setUser(emailUser);
                accountB.setUser(emailUser);
                givenValidTransferSetup();

                accountService.transfer(ACC_A, ACC_B, 100.0);

                verifyNoInteractions(smsService);
        }

        @Test
        @DisplayName("remove an account with a balance greater than zero should throw IllegalArgumentException")
        public void removeAccountWithBalanceGreaterThanZeroShouldThrowIllegalArgumentException() {
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));

                assertThatThrownBy(() -> accountService.rm(ACC_A)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Cannot delete account with non-zero balance");
        }

        @Test
        @DisplayName("remove an account with a balance of zero shoud delete account")
        public void removeAccountWithBalanceZeroShouldDeleteAccount(){
                Account zeroBalanceAccount = new Account(ACC_A, Account.AccountType.CHECKING, 0.0);
                when(accountRepository.findByAccountNumber(any())).thenReturn(Optional.of(zeroBalanceAccount));
                accountService.rm(ACC_A);
                verify(accountRepository).delete(zeroBalanceAccount);
        }

        
        @Test
        @DisplayName("getAccount - returns the account when it exists")
        void getAccount_ExistingAccount_returnsAccount() {
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));

                Account result = accountService.getAccount(ACC_A);

                assertThat(result).isNotNull();
                assertThat(result.getAccountNumber()).isEqualTo(ACC_A);
                assertThat(result).isEqualTo(accountA);
                verify(accountRepository).findByAccountNumber(ACC_A);
        }

        @Test
        @DisplayName("getAccount - throws IllegalArgumentException when account does not exist")
        void getAccount_nonExistingAccount_throwsException() {
                when(accountRepository.findByAccountNumber(ACC_MISSING)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> accountService.getAccount(ACC_MISSING))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Account not found");
        }
}

