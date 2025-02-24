package service;

import chess.ChessGame;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import dto.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Set;

public class ServiceTests {

    private static UserService userService;
    private static GameService gameService;
    private static AuthService authService;
    private static MemoryUserAccess userAccess;
    private static MemoryGameAccess gameAccess;
    private static MemoryAuthAccess authAccess;

    private static final UserData normalUser = new UserData("username", "password", "email");

    @BeforeAll
    public static void init() {
        userService = new UserService();
        gameService = new GameService();
        authService = new AuthService();
        userAccess = new MemoryUserAccess();
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        userService.setAccess(userAccess, authAccess);
        gameService.setGameAccess(gameAccess, authAccess);
        authService.setAuthAccess(userAccess, gameAccess, authAccess);
    }

    @AfterEach
    public void close() {
        userAccess.clear();
        gameAccess.clear();
        authAccess.clear();
    }

    @Test
    @DisplayName("Clear all data")
    public void clearAllData() {
        GameData game = new GameData(1, "white", "black", "game1", new ChessGame());
        AuthData auth = new AuthData("username", "authToken");
        userAccess.addUser(normalUser);
        gameAccess.addGame(game);
        authAccess.addAuth(auth);

        ClearResponse expected = new ClearResponse(null);
        ClearResponse result = authService.clear();

        Assertions.assertEquals(expected, result);

        Assertions.assertEquals(0, userAccess.getAllUsers().size());
        Assertions.assertEquals(0, gameAccess.getAllGames().size());
        Assertions.assertEquals(0, authAccess.getAllAuth().size());
    }

    @Test
    @DisplayName("Register normal user")
    public void registerNormalUser() {
        authService.register(new RegisterRequest(normalUser));

        Assertions.assertTrue(userAccess.getAllUsers().contains(normalUser));
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
        userAccess.addUser(normalUser);
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
        userAccess.addUser(normalUser);
        UserData user = new UserData("username", "wrong password", null);
        String expected = "Error: unauthorized";
        LoginResponse result = userService.login(new LoginRequest(user));

        Assertions.assertEquals(expected, result.message());
        Assertions.assertNull(result.username(), result.authToken());
    }

    @Test
    @DisplayName("Normal logout")
    public void normalLogout() {
        userAccess.addUser(normalUser);
        authAccess.addAuth(new AuthData("username", "token"));

        LogoutResponse expected = new LogoutResponse(null);
        LogoutResponse result = userService.logout(new LogoutRequest("token"));

        Assertions.assertEquals(expected, result);
        Assertions.assertTrue(authAccess.getAllAuth().isEmpty());
    }

    @Test
    @DisplayName("Unauthorized logout")
    public void unauthorizedLogout() {
        userAccess.addUser(normalUser);
        authAccess.addAuth(new AuthData("username", "token"));

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

        Assertions.assertEquals(1, gameAccess.getAllGames().size());
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Unauthorized create game")
    public void unauthorizedCreateGame() {
        authService.register(new RegisterRequest(normalUser));

        CreateGameResponse expected = new CreateGameResponse(null, "Error: unauthorized");
        CreateGameResponse result = gameService.createGame(new CreateGameRequest("game1", "wrong token"));

        Assertions.assertEquals(0, gameAccess.getAllGames().size());
        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("Valid list games")
    public void validListGames() {
        userAccess.addUser(normalUser);
        authAccess.addAuth(new AuthData("username", "authToken"));
        Set<GameData> games = Set.of(
                new GameData(1, "white1", "black1", "game1", new ChessGame()),
                new GameData(2, "white2", "black2", "game2", new ChessGame()),
                new GameData(3, "white3", "black3", "game3", new ChessGame()),
                new GameData(4, "white4", "black4", "game4", new ChessGame()),
                new GameData(5, "white5", "black5", "game5", new ChessGame())
        );
        for (GameData game : games) {
            gameAccess.addGame(game);
        }

        ListGamesResponse expected = new ListGamesResponse(games, null);
        ListGamesResponse result = gameService.listGames(new ListGamesRequest("authToken"));

        Assertions.assertEquals(expected.games(), result.games());
    }

    @Test
    @DisplayName("Unauthorized list games")
    public void unauthorizedListGames() {
        userAccess.addUser(normalUser);
        authAccess.addAuth(new AuthData("username", "authToken"));
        Set<GameData> games = Set.of(
                new GameData(1, "white1", "black1", "game1", new ChessGame()),
                new GameData(2, "white2", "black2", "game2", new ChessGame()),
                new GameData(3, "white3", "black3", "game3", new ChessGame()),
                new GameData(4, "white4", "black4", "game4", new ChessGame()),
                new GameData(5, "white5", "black5", "game5", new ChessGame())
        );
        for (GameData game : games) {
            gameAccess.addGame(game);
        }

        ListGamesResponse expected = new ListGamesResponse(null, "Error: unauthorized");
        ListGamesResponse result = gameService.listGames(new ListGamesRequest("wrong authToken"));

        Assertions.assertEquals(expected.games(), result.games());
    }
}
