package dataaccess;

import chess.ChessGame;
import dataaccess.sql.SqlAuthAccess;
import dataaccess.sql.SqlDatabaseManager;
import dataaccess.sql.SqlGameAccess;
import dataaccess.sql.SqlUserAccess;
import dto.ListGamesResponse;
import dto.LoginResponse;
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
    public static void init() {
        normalUser = new UserData("username", "password", "email");
        normalAuth = new AuthData("username", "authToken", null);
        normalGame = new GameData(1, null, null, "game1", new ChessGame());
        sqlDbManager = new SqlDatabaseManager();
    }

    @AfterEach
    public void clearAll() {
        sqlDbManager.clearAll();
    }

    @Test
    @DisplayName("Configure User Database")
    public void configureUserDatabaseTest() {
        Assertions.assertDoesNotThrow(SqlUserAccess::new);
    }

    @Test
    @DisplayName("Add a Normal User to User Database")
    public void addNormalUserToDatabaseTest() {
        Assertions.assertNull(sqlDbManager.userAccess().addUser(normalUser));
    }

    @Test
    @DisplayName("Add a Duplicate Username to User Database")
    public void addDuplicateUsernameTest() {
        sqlDbManager.userAccess().clear();
        sqlDbManager.userAccess().addUser(normalUser);

        String expected = "Failed to add user: username already exists";
        Assertions.assertEquals(expected, sqlDbManager.userAccess().addUser(normalUser));
    }

    @Test
    @DisplayName("Add a Duplicate Email to User Database")
    public void addDuplicateEmailTest() {
        sqlDbManager.userAccess().clear();
        sqlDbManager.userAccess().addUser(normalUser);
        UserData duplicateEmail = new UserData("normalUser", "password", "email");

        String expected = "Failed to add user: email already exists";
        Assertions.assertEquals(expected, sqlDbManager.userAccess().addUser(duplicateEmail));
    }

    @Test
    @DisplayName("Clear User Database")
    public void clearUserTableTest() {
        sqlDbManager.userAccess().addUser(normalUser);
        Assertions.assertNull(sqlDbManager.userAccess().clear());
    }

    @Test
    @DisplayName("Configure Game Database")
    public void configureGameDatabaseTest() {
        Assertions.assertDoesNotThrow(SqlGameAccess::new);
    }

    @Test
    @DisplayName("Configure Auth Database")
    public void configureAuthDatabaseTest() {
        Assertions.assertDoesNotThrow(SqlAuthAccess::new);
    }

    @Test
    @DisplayName("Get User from Database by Username")
    public void normalGetUserTest() {
        sqlDbManager.userAccess().addUser(normalUser);

        String expected = normalUser.username();
        String actual = sqlDbManager.userAccess().getUserByUsername(normalUser.username()).username();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get User from Database With Bad Username")
    public void wrongUsernameGetUserTest() {
        sqlDbManager.userAccess().addUser(normalUser);
        AuthData result = sqlDbManager.userAccess().getUserByUsername("wrong username");

        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("Get User from Database by Credentials")
    public void getUserByCredentialsTest() {
        sqlDbManager.userAccess().addUser(normalUser);

        String expected = normalUser.username();
        LoginResponse actual = sqlDbManager.userAccess().getUser(normalUser.username(), normalUser.password()
        );

        Assertions.assertEquals(expected, actual.username());
    }

    @Test
    @DisplayName("Get User with Wrong Password")
    public void getUserWithWrongPasswordTest() {
        sqlDbManager.userAccess().addUser(normalUser);
        LoginResponse result = sqlDbManager.userAccess().getUser("username", "wrongPassword");

        Assertions.assertNull(result.username());
        Assertions.assertNull(result.authToken());
        Assertions.assertNotNull(result.message());
    }

    @Test
    @DisplayName("Add Auth to Database")
    public void addAuthToDatabaseTest() {
        Assertions.assertNull(sqlDbManager.authAccess().addAuth(normalAuth));
    }

    @Test
    @DisplayName("Add Existing Auth Token to Database")
    public void addExistingAuthToDatabaseTest() {
        sqlDbManager.authAccess().addAuth(normalAuth);
        String result = sqlDbManager.authAccess().addAuth(normalAuth);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("Failed to add auth"));
    }

    @Test
    @DisplayName("Get Auth From Database")
    public void getAuthFromDatabaseTest() {
        sqlDbManager.authAccess().addAuth(normalAuth);

        Assertions.assertNotNull(sqlDbManager.authAccess().getAuth(normalAuth.authToken()));
    }

    @Test
    @DisplayName("Get Auth With Bad Auth Token")
    public void getAuthWithBadTokenTest() {
        sqlDbManager.authAccess().addAuth(normalAuth);
        AuthData result = sqlDbManager.authAccess().getAuth("wrong token");

        Assertions.assertNull(result.username());
        Assertions.assertNull(result.authToken());
        Assertions.assertEquals("Error: Wrong Auth Token", result.message());
    }

    @Test
    @DisplayName("Add Game to Database")
    public void addGameToDatabaseTest() {
        Assertions.assertEquals("1", sqlDbManager.gameAccess().addGame(normalGame));
    }

    @Test
    @DisplayName("Add Game to Database")
    public void addBadGameToDatabaseTest() {
        GameData badGame = new GameData(null, null, null, null, new ChessGame());
        String result = sqlDbManager.gameAccess().addGame(badGame);

        Assertions.assertEquals("Error: no game name provided", result);
    }

    @Test
    @DisplayName("Normal Get Games From Database")
    public void normalGetAllGamesTest() {
        GameData game2 = new GameData(
                2, null, null, "game2", new ChessGame()
        );
        GameData game3 = new GameData(
                3, null, null, "game3", new ChessGame()
        );

        sqlDbManager.gameAccess().addGame(normalGame);
        sqlDbManager.gameAccess().addGame(game2);
        sqlDbManager.gameAccess().addGame(game3);

        ListGamesResponse response = sqlDbManager.gameAccess().getAllGames();

        Assertions.assertNull(response.message());
        Assertions.assertEquals(3, sqlDbManager.gameAccess().getAllGames().games().size()
        );
    }

    @Test
    @DisplayName("Get All Games With Empty Database")
    public void emptyDatabaseGetAllGamesTestTest() {
        ListGamesResponse result = sqlDbManager.gameAccess().getAllGames();

        Assertions.assertTrue(result.games().isEmpty());
    }

    @Test
    @DisplayName("Normal Update Game")
    public void normalUpdateGameTest() {
        sqlDbManager.gameAccess().addGame(normalGame);

        Assertions.assertNull(
                sqlDbManager.gameAccess().updateGame("WHITE", 1, normalUser.username())
        );
        Assertions.assertTrue(
                sqlDbManager.gameAccess().getAllGames().games().stream()
                        .anyMatch(game -> Objects.equals(game.whiteUsername(), normalUser.username()))
        );
    }

    @Test
    @DisplayName("Update Game Bad PlayerColor")
    public void badPlayerColorUpdateGameTest() {
        sqlDbManager.gameAccess().addGame(normalGame);
        sqlDbManager.gameAccess().updateGame("WHITE", 1, "username");

        Assertions.assertEquals(
                "Error: Color already taken",
                sqlDbManager.gameAccess().updateGame("WHITE", 1, "username")
        );
    }
}
