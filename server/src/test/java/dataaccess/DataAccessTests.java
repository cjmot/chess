package dataaccess;

import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DataAccessTests {

    private static UserData normalUser = new UserData("username", "password", "email");
    private static SqlDatabaseManager sqlDbManager;

    @BeforeAll
    public static void init() throws ResponseException {
        normalUser = new UserData("username", "password", "email");
        sqlDbManager = new SqlDatabaseManager();
    }


    @Test
    @DisplayName("Configure User Database")
    public void configureUserDatabase() {
        Assertions.assertDoesNotThrow(SqlDatabaseManager::new);
    }

    @Test
    @DisplayName("Add a user to User Database")
    public void addUserToDatabase() {
        Assertions.assertNull(sqlDbManager.userAccess().addUser(normalUser));
    }
}
