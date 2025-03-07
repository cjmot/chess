package dataaccess;

import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;

public class DataAccessTests {

    private static UserData normalUser = new UserData("username", "password", "email");
    private static SqlDatabaseManager sqlDbManager;

    @BeforeAll
    public static void init() throws ResponseException {
        normalUser = new UserData("username", "password", "email");
        sqlDbManager = new SqlDatabaseManager();
    }

    @AfterAll
    public static void close() {
        sqlDbManager.userAccess().clear();
    }

    @Test
    @DisplayName("Configure User Database")
    public void configureUserDatabase() {
        Assertions.assertDoesNotThrow(SqlDatabaseManager::new);
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
}
