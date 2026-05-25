package es.codeurjc.unit;

public class AccountServiceTestConstants {

    public static final String ACC_A = "ES0000000001";
    public static final String ACC_B = "ES0000000002";
    public static final String ACC_MISSING = "ES9999999999";

    public static final String ACC_C = "ES0000000003";
    public static final String ACC_ZERO_BALANCE = "ES0000000010";

    public static final String USER1_NAME = "user1";
    public static final String USER2_NAME = "user2";
    public static final String USER3_NAME = "user3";
    public static final String USER_EMAIL = "user2email";
    public static final String USER_SMS = "user2sms";

    public static final String PASSWORD = "pass";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String EMAIL_1 = "user1@test.com";
    public static final String EMAIL_2 = "user2@test.com";

    public static final String PHONE_1 = "600000000";
    public static final String PHONE_2 = "611111111";

    public static final double INITIAL_BALANCE_A = 500.0;
    public static final double INITIAL_BALANCE_B = 200.0;
    public static final double INITIAL_BALANCE_C = 500.0;

    public static final int NEGATIVE_AMOUNT = -200;
    public static final int ZERO_AMOUNT = 0;
    public static final int MICRO_AMOUNT = 50;
    public static final int SMALL_AMOUNT = 100;
    public static final int OVER_AMOUNT = 20000;
    public static final int EXTRA_OVER_AMOUNT = 60000;
    public static final double MICRO_AMOUNT_D = 50.0;
    public static final double MEDIUM_AMOUNT_D = 4500.0;

    public static final double STANDARD_AMOUNT_D = 600.0;
    public static final double OVER_LIMIT_D = 20001.0;

    public static final String MSG_AMOUNT_POSITIVE = "Amount must be positive";
    public static final String MSG_LIMIT_WITHDRAW = "Amount exceeds maximum withdrawal limit";
    public static final String MSG_LIMIT_TRANSFER = "Amount exceeds maximum transfer limit";
    public static final String MSG_LIMIT_DEPOSIT = "Amount exceeds maximum deposit limit";
    public static final String MSG_INSUFFICIENT_FUNDS = "Insufficient funds";
    public static final String MSG_ACCOUNT_NOT_FOUND = "Account not found";
    public static final String MSG_CANNOT_DELETE = "Cannot delete account with non-zero balance";

    public static final String MSG_24_HOUR_LIMIT = "Operation cancelled: Total withdrawals in the last 24 hours would exceed the 5000 limit";

    public static final String MSG_BANNED_WITHDRAWAL = "Banned user cannot withdraw money";
    public static final String MSG_BANNED_DEPOSIT = "Banned user cannot deposit money";
    public static final String MSG_BANNED_TRANSFER_SOURCE = "Banned user cannot transfer money";
    public static final String MSG_BANNED_TRANSFER_DESTINATION = "Cannot transfer money to a banned user";
    public static final String MSG_UNDER_AGE = "User must be 18 years old to make transfers";

    public static final String TITLE_WITHDRAWAL_CONFIRMATION = "Withdrawal Confirmation";
    public static final String TITLE_WITHDRAWAL = "Withdrawal";
    public static final String TITLE_DEPOSIT = "Deposit Confirmation";
    public static final String TITLE_TRANSFER_SENT = "Transfer Sent";
    public static final String TITLE_TRANSFER_RECEIVED = "Transfer Received";

    public static final String TEST_DESC = "Test";
    public static final String PADDEL_DESC = "Padel match";
    public static final String DEPOSIT_A_LOT_DESC = "Deposit a lot of money";
    public static final String DEPOSIT_ZERO_DESC = "Deposit zero amount";
    public static final String DEPOSIT_NEGATIVE_DESC = "Deposit negative amount";
    public static final String WITHDRAW_A_LOT_DESC = "Withdraw a lot of money";
    public static final String WITHDRAW_ZERO_DESC = "Withdraw zero amount";
    public static final String WITHDRAW_NEGATIVE_DESC = "Withdraw negative amount";



    public static final String WITHDRAW_FORMAT="Withdrawal of %.2f EUR. New balance: %.2f EUR";
    public static final String DEPOSIT_EMAIL_FORMAT="Deposit of %.2f EUR. New balance: %.2f EUR";
    public static final String DEPOSIT_SMS_FORMAT="Deposit: %.2f EUR. Balance: %.2f EUR";
    public static final String TRANSFER_TO_FORMAT="Transfer of %.2f EUR to %s. New balance: %.2f EUR";
    public static final String TRANSFER_FROM_FORMAT="Transfer of %.2f EUR from %s. New balance: %.2f EUR";
}
