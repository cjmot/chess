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
    @DisplayName("Add a User to User Database")
    public void addUserToDatabase() {
        Assertions.assertNull(sqlDbManager.userAccess().addUser(normalUser));
    }

    @Test
    @DisplayName("Clear User Database")
    public void clearUserTable() {
        sqlDbManager.userAccess().addUser(normalUser);
        Assertions.assertNull(sqlDbManager.userAccess().clear());
    }
}
