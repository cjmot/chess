package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

public class DataAccessTests {

    private static UserData normalUser;
    private static AuthData normalAuth;
    private static SqlDatabaseManager sqlDbManager;

    @BeforeAll
    public static void init() throws ResponseException {
        normalUser = new UserData("username", "password", "email");
        normalAuth = new AuthData("username", "authToken");
        sqlDbManager = new SqlDatabaseManager();
    }

    @AfterEach
    public void clearAll() {
        sqlDbManager.clearAll();

    }

    @Test
    @DisplayName("Configure User Database")
    public void configureUserDatabase() {
        Assertions.assertDoesNotThrow(SqlUserAccess::new);
    }

    @Test
    @DisplayName("Add a Normal User to User Database")
    public void addNormalUserToDatabase() {
        Assertions.assertNull(sqlDbManager.userAccess().addUser(normalUser));
    }

    @Test
    @DisplayName("Add a Duplicate Username to User Database")
    public void addDuplicateUsername() {
        sqlDbManager.userAccess().clear();
        sqlDbManager.userAccess().addUser(normalUser);

        String expected = "Failed to add user: username already exists";
        Assertions.assertEquals(expected, sqlDbManager.userAccess().addUser(normalUser));
    }

    @Test
    @DisplayName("Add a Duplicate Email to User Database")
    public void addDuplicateEmail() {
        sqlDbManager.userAccess().clear();
        sqlDbManager.userAccess().addUser(normalUser);
        UserData duplicateEmail = new UserData("normalUser", "password", "email");

        String expected = "Failed to add user: email already exists";
        Assertions.assertEquals(expected, sqlDbManager.userAccess().addUser(duplicateEmail));
    }

    @Test
    @DisplayName("Clear User Database")
    public void clearUserTable() {
        sqlDbManager.userAccess().addUser(normalUser);
        Assertions.assertNull(sqlDbManager.userAccess().clear());
    }

    @Test
    @DisplayName("Configure Game Database")
    public void configureGameDatabase() {
        Assertions.assertDoesNotThrow(SqlGameAccess::new);
    }

    @Test
    @DisplayName("Configure Auth Database")
    public void configureAuthDatabase() {
        Assertions.assertDoesNotThrow(SqlAuthAccess::new);
    }

    @Test
    @DisplayName("Get User from Database")
    public void normalGetUser() throws ResponseException {
        sqlDbManager.userAccess().addUser(normalUser);

        Assertions.assertEquals(normalUser.username(), sqlDbManager.userAccess().getUserByUsername(normalUser.username()));
    }
}
