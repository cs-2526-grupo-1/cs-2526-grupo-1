package es.codeurjc.e2e;

public class E2ETestConstants {

    public static final String BASE_URL = "http://localhost:";

    public static final String USER1_USERNAME = "testuser1";
    public static final String USER1_PASSWORD = "testuser1";
    public static final String USER1_EMAIL = "testuser1@test.com";

    public static final String USER2_USERNAME = "testuser2";
    public static final String USER2_PASSWORD = "testuser2";
    public static final String USER2_EMAIL = "testuser2@test.com";

    public static final String ACCOUNT_1_CHECKING = "ES1111111111";
    public static final String ACCOUNT_1_SAVINGS = "ES2222222222";
    public static final String ACCOUNT_2_CHECKING = "ES3333333333";

    public static final Double INITIAL_BALANCE_ACCOUNT1_CHECKING = 5000.0;
    public static final Double INITIAL_BALANCE_ACCOUNT1_SAVINGS = 3000.0;
    public static final Double INITIAL_BALANCE_ACCOUNT2 = 2000.0;

    public static final String ERROR_NEGATIVE_AMOUNT = "Amount must be positive";
    public static final String ERROR_EXCEEDS_LIMIT = "Amount exceeds maximum transfer limit";

    public static final int NEGATIVE_AMOUNT = -50;
    public static final int EXCEEDING_AMOUNT = 30000;

    public static final String ID_USERNAME = "username";
    public static final String ID_PASSWORD = "password";
    public static final String ID_LOGIN_BUTTON = "loginButton";

    public static final String ID_FROM_ACCOUNT = "fromAccount";
    public static final String ID_TO_ACCOUNT = "toAccount";
    public static final String ID_AMOUNT = "amount";
    public static final String ID_TRANSFER_BUTTON = "transferButton";

    public static final String ID_ERROR_MESSAGE = "errorMessage";
    public static final String ID_BALANCE_PREFIX = "balance-";

    public static final String PATH_LOGIN = "/login";
    public static final String PATH_TRANSFER = "/transfer";
    public static final String PATH_DASHBOARD = "/dashboard";
}