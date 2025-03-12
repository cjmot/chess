package service;

import chess.ChessGame;
import dataaccess.sql.SqlDatabaseManager;
import dto.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;

public class ServiceTests {

    private static UserService userService;
    private static GameService gameService;
    private static AuthService authService;
    private static SqlDatabaseManager dbManager;

    private static UserData normalUser;

    @BeforeAll
    public static void init() {
        dbManager = new SqlDatabaseManager();
        userService = new UserService(dbManager);
        gameService = new GameService(dbManager);
        authService = new AuthService(dbManager);
        normalUser = new UserData("username", "password", "email");
    }

    @AfterEach
    public void close() {
        dbManager.userAccess().clear();
        dbManager.gameAccess().clear();
        dbManager.authAccess().clear();
    }

    @Test
    @DisplayName("Clear all data")
    public void clearAllData() {
        GameData game = new GameData(1, "white", "black", "game1", new ChessGame());
        AuthData auth = new AuthData("username", "authToken", null);
        dbManager.userAccess().addUser(normalUser);
        dbManager.gameAccess().addGame(game);
        dbManager.authAccess().addAuth(auth);

        ClearResponse expected = new ClearResponse(null);
        ClearResponse result = authService.clear();

        Assertions.assertEquals(expected, result);

        Assertions.assertEquals(0, dbManager.gameAccess().getAllGames().games().size());
    }

    @Test
    @DisplayName("Register normal user")
    public void registerNormalUser() {
        authService.register(new RegisterRequest(normalUser));
        String actual = dbManager.userAccess().getUserByUsername(normalUser.username()).username();
        Assertions.assertEquals(normalUser.username(), actual);
    }

    @Test
    @DisplayName("Username already exists")
    public void registerUserExists() {
        authService.register(new RegisterRequest(normalUser));
        RegisterResponse expected = new RegisterResponse(
                null, null, "Error: already taken");
        RegisterResponse result = authService.register(new RegisterRequest(normalUser));

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Normal login")
    public void normalLogin() {
        dbManager.userAccess().addUser(normalUser);
        UserData user = new UserData("username", "password", null);
        String expected = "username";
        LoginResponse result = userService.login(new LoginRequest(user));

        Assertions.assertNull(result.message());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals(expected, result.username());
    }

    @Test
    @DisplayName("Unauthorized login")
    public void unauthorizedLogin() {
        dbManager.userAccess().addUser(normalUser);
        UserData user = new UserData("username", "wrong password", null);
        String expected = "Error: unauthorized";
        LoginResponse result = userService.login(new LoginRequest(user));

        Assertions.assertEquals(expected, result.message());
        Assertions.assertNull(result.username(), result.authToken());
    }

    @Test
    @DisplayName("Normal logout")
    public void normalLogout() {
        dbManager.userAccess().addUser(normalUser);
        dbManager.authAccess().addAuth(new AuthData("username", "token", null));

        LogoutResponse expected = new LogoutResponse(null);
        LogoutResponse result = userService.logout(new LogoutRequest("token"));

        Assertions.assertEquals(expected, result);
        Assertions.assertEquals("Error: Wrong Auth Token", dbManager.authAccess().getAuth("token").message());
    }

    @Test
    @DisplayName("Unauthorized logout")
    public void unauthorizedLogout() {
        dbManager.userAccess().addUser(normalUser);
        dbManager.authAccess().addAuth(new AuthData("username", "token", null));

        String expected = "Error: unauthorized";
        LogoutResponse result = userService.logout(new LogoutRequest("wrong token"));

        Assertions.assertEquals(expected, result.message());
    }

    @Test
    @DisplayName("Valid create game")
    public void validCreateGame() {
        RegisterResponse response = authService.register(new RegisterRequest(normalUser));

        CreateGameResponse expected = new CreateGameResponse(1, null);
        CreateGameResponse result = gameService.createGame(new CreateGameRequest("game1", response.authToken()));

        Assertions.assertEquals(1, dbManager.gameAccess().getAllGames().games().size());
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Unauthorized create game")
    public void unauthorizedCreateGame() {
        authService.register(new RegisterRequest(normalUser));

        CreateGameResponse expected = new CreateGameResponse(null, "Error: unauthorized");
        CreateGameResponse result = gameService.createGame(new CreateGameRequest("game1", "wrong token"));

        Assertions.assertEquals(0, dbManager.gameAccess().getAllGames().games().size());
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Valid list games")
    public void validListGames() {
        dbManager.userAccess().addUser(normalUser);
        dbManager.authAccess().addAuth(new AuthData("username", "authToken", null));
        Collection<GameData> games = List.of(
                new GameData(1, "white1", "black1", "game1", new ChessGame()),
                new GameData(2, "white2", "black2", "game2", new ChessGame()),
                new GameData(3, "white3", "black3", "game3", new ChessGame()),
                new GameData(4, "white4", "black4", "game4", new ChessGame()),
                new GameData(5, "white5", "black5", "game5", new ChessGame())
        );
        for (GameData game : games) {
            dbManager.gameAccess().addGame(game);
        }

        ListGamesResponse expected = new ListGamesResponse(games, null);
        ListGamesResponse result = gameService.listGames(new ListGamesRequest("authToken"));

        Assertions.assertEquals(expected.games().size(), result.games().size());
    }

    @Test
    @DisplayName("Unauthorized list games")
    public void unauthorizedListGames() {
        dbManager.userAccess().addUser(normalUser);
        dbManager.authAccess().addAuth(new AuthData("username", "authToken", null));
        Collection<GameData> games = List.of(
                new GameData(1, "white1", "black1", "game1", new ChessGame()),
                new GameData(2, "white2", "black2", "game2", new ChessGame()),
                new GameData(3, "white3", "black3", "game3", new ChessGame()),
                new GameData(4, "white4", "black4", "game4", new ChessGame()),
                new GameData(5, "white5", "black5", "game5", new ChessGame())
        );
        for (GameData game : games) {
            dbManager.gameAccess().addGame(game);
        }

        ListGamesResponse expected = new ListGamesResponse(null, "Error: unauthorized");
        ListGamesResponse result = gameService.listGames(new ListGamesRequest("wrong authToken"));

        Assertions.assertEquals(expected.games(), result.games());
    }

    @Test
    @DisplayName("Normal join game")
    public void normalJoinGame() {
        dbManager.userAccess().addUser(normalUser);
        dbManager.authAccess().addAuth(new AuthData("username", "authToken", null));
        gameService.createGame(new CreateGameRequest("game1", "authToken"));

        JoinGameResponse expected = new JoinGameResponse(null);
        JoinGameResponse result = gameService.joinGame(new JoinGameRequest("WHITE", 1, "authToken"));

        Assertions.assertEquals(expected, result);
        Assertions.assertTrue(
                dbManager.gameAccess().getAllGames().games().stream().anyMatch(
                        game -> game.whiteUsername().equals("username"))
        );
    }

    @Test
    @DisplayName("Unauthorized join game")
    public void unauthorizedJoinGame() {
        dbManager.userAccess().addUser(normalUser);
        dbManager.authAccess().addAuth(new AuthData("username", "authToken", null));
        gameService.createGame(new CreateGameRequest("game1", "authToken"));

        JoinGameResponse expected = new JoinGameResponse("Error: unauthorized");
        JoinGameResponse result = gameService.joinGame(new JoinGameRequest("WHITE", 1, "wrong authToken"));

        Assertions.assertEquals(expected, result);
    }
}
