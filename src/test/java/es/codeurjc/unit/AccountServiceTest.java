package es.codeurjc.unit;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.codeurjc.model.Account;
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
                verify(emailService).sendNotification(any(), any(), any(), any());
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
                verify(smsService).sendNotification(any(), any(), any(), any());
                verify(emailService, never()).sendNotification(any(), any(), any(), any());
        }

        
        @Test
        @DisplayName("getAccount - returns the account when it exists")
        void getAccount_ExistingAccount_returnsAccount() {
                //Se establece devoluc de cuenta asociada a numero de cuenta
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));

                Account result = accountService.getAccount(ACC_A); //internamente llama a accountRepository.findByAccountNumber(ACC_A) 

                assertThat(result).isNotNull();
                assertThat(result.getAccountNumber()).isEqualTo(ACC_A); //mismo numero de cuenta
                assertThat(result).isEqualTo(accountA); //misma cuenta
                verify(accountRepository).findByAccountNumber(ACC_A); //verificamos llamada método por parte del mock
        }

        @Test
        @DisplayName("getAccount - throws IllegalArgumentException when account does not exist")
        void getAccount_nonExistingAccount_throwsException() {
                when(accountRepository.findByAccountNumber(ACC_MISSING)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> accountService.getAccount(ACC_MISSING))
                                .isInstanceOf(IllegalArgumentException.class) //tipo excecpcion
                                .hasMessage("Account not found"); //mensaje error
        }

        
        //transfer 1st part

        @Test
        @DisplayName("transfer - throws when amount zero or negative")
        void transfer_invalidAmount_throwsException() {
                assertThatThrownBy(() -> accountService.transfer(ACC_A, ACC_B, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be positive");

                assertThatThrownBy(() -> accountService.transfer(ACC_A, ACC_B, -67))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be positive");
        }

        @Test
        @DisplayName("transfer - throws when amount exceeds limit")
        void transfer_amountExceedsLimit_throwsException() {
                assertThatThrownBy(() -> accountService.transfer(ACC_A, ACC_B, 20001.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount exceeds maximum transfer limit");
        }

        @Test
        @DisplayName("transfer - throws when source and destination are the same account")
        void transfer_sameAccount_throwsException() {

                // Purueba con misma instancia de misma cuenta (pasa el test)
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));

                assertThatThrownBy(() -> accountService.transfer(ACC_A, ACC_A, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transfer to same account");


                // Prueba con nueva instancia de la misma cuenta (debido a implementacion con ==, no se pasará el test, ya que no lanzará excepción)
                String ACC_A2 = new String(ACC_A);
                Account accountA2 = new Account(ACC_A2, Account.AccountType.CHECKING, 500.0);

                when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(accountA), Optional.of(accountA2)); //Instancia diferente de misma cuenta
                
                assertThatThrownBy(() -> accountService.transfer(ACC_A, ACC_A2, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transfer to same account");
        }

        @Test
        @DisplayName("transfer - throws when source account has insufficient funds")
        void transfer_insufficientFunds_throwsException() {
                // Cuenta A tiene 500€ iniciales en el setUp(), intentamos transferir 600€
                when(accountRepository.findByAccountNumber(ACC_A)).thenReturn(Optional.of(accountA));
                when(accountRepository.findByAccountNumber(ACC_B)).thenReturn(Optional.of(accountB));

                assertThatThrownBy(() -> accountService.transfer(ACC_A, ACC_B, 600.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient funds");
        }
      
}
