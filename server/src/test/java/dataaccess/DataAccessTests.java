package dataaccess;

import chess.ChessGame;
import dataaccess.sql.SqlAuthAccess;
import dataaccess.sql.SqlDatabaseManager;
import dataaccess.sql.SqlGameAccess;
import dataaccess.sql.SqlUserAccess;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Objects;

public class DataAccessTests {

    private static UserData normalUser;
    private static AuthData normalAuth;
    private static GameData normalGame;
    private static SqlDatabaseManager sqlDbManager;

    @BeforeAll
    public static void init() throws ResponseException {
        normalUser = new UserData("username", "password", "email");
        normalAuth = new AuthData("username", "authToken");
        normalGame = new GameData(1, null, null, "game1", new ChessGame());
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
    @DisplayName("Get User from Database by Username")
    public void normalGetUser() throws ResponseException {
        sqlDbManager.userAccess().addUser(normalUser);

        String expected = normalUser.username();
        String actual = sqlDbManager.userAccess().getUserByUsername(normalUser.username());

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get User from Database by Credentials")
    public void getUserByCredentials() throws ResponseException {
        sqlDbManager.userAccess().addUser(normalUser);

        String expected = normalUser.username();
        String actual = sqlDbManager.userAccess().getUser(normalUser.username(), normalUser.password());

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Add Auth to Database")
    public void addAuthToDatabase() {
        Assertions.assertNull(sqlDbManager.authAccess().addAuth(normalAuth));
    }

    @Test
    @DisplayName("Get Auth From Database")
    public void getAuthFromDatabase() throws ResponseException {
        sqlDbManager.authAccess().addAuth(normalAuth);

        Assertions.assertNotNull(sqlDbManager.authAccess().getAuth(normalAuth.authToken()));
    }

    @Test
    @DisplayName("Add Game to Database")
    public void addGameToDatabase() {
        Assertions.assertNull(sqlDbManager.gameAccess().addGame(normalGame));
    }

    @Test
    @DisplayName("Get Game From Database")
    public void getAllGames() throws ResponseException {
        GameData game2 = new GameData(
                2, null, null, "game2", new ChessGame()
        );
        GameData game3 = new GameData(
                3, null, null, "game3", new ChessGame()
        );

        sqlDbManager.gameAccess().addGame(normalGame);
        sqlDbManager.gameAccess().addGame(game2);
        sqlDbManager.gameAccess().addGame(game3);

        Assertions.assertEquals(3, sqlDbManager.gameAccess().getAllGames().size());
    }

    @Test
    @DisplayName("Update Game")
    public void updateGame() throws ResponseException {
        sqlDbManager.gameAccess().addGame(normalGame);

        Assertions.assertNull(
                sqlDbManager.gameAccess().updateGame("WHITE", 1, normalUser.username())
        );
        Assertions.assertTrue(
                sqlDbManager.gameAccess().getAllGames().stream()
                        .anyMatch(game -> Objects.equals(game.whiteUsername(), normalUser.username()))
        );
    }
}
