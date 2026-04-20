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
    public static final int SMALL_TRANSFER = 100;
    public static final double LARGE_TRANSFER = 600.0;
    public static final double OVER_LIMIT = 20001.0;

    public static final String MSG_AMOUNT_POSITIVE = "Amount must be positive";
    public static final String MSG_LIMIT_WITHDRAW = "Amount exceeds maximum withdrawal limit";
    public static final String MSG_LIMIT_DEPOSIT = "Amount exceeds maximum deposit limit";
    public static final String MSG_INSUFFICIENT_FUNDS = "Insufficient funds";
    public static final String MSG_ACCOUNT_NOT_FOUND = "Account not found";
    public static final String MSG_CANNOT_DELETE = "Cannot delete account with non-zero balance";

    public static final String TITLE_WITHDRAWAL = "Withdrawal Confirmation";
    public static final String TITLE_DEPOSIT = "Deposit Confirmation";
    public static final String TITLE_TRANSFER_SENT = "Transfer Sent";
    public static final String TITLE_TRANSFER_RECEIVED = "Transfer Received";

    public static final String TEST_DESCRIPTION = "Test";
    public static final String PADDEL_DESC = "Padel match";
}