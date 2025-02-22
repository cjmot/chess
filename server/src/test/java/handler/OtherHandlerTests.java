package handler;

import chess.ChessGame;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import dto.*;
import model.*;
import org.junit.jupiter.api.*;
import server.OtherHandler;
import service.*;

public class OtherHandlerTests {
    private static OtherHandler otherHandler;
    private static UserService userService;
    private static GameService gameService;
    private static AuthService authService;
    private static MemoryUserAccess userAccess;
    private static MemoryGameAccess gameAccess;
    private static MemoryAuthAccess authAccess;

    private static UserData normalUser = new UserData("username", "password", "email");

    @BeforeAll
    public static void init() {
        otherHandler = new OtherHandler();
        userService = new UserService();
        gameService = new GameService();
        authService = new AuthService();
        userAccess = new MemoryUserAccess();
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        userService.setUserAccess(userAccess);
        gameService.setGameAccess(gameAccess);
        authService.setAuthAccess(authAccess);
        otherHandler.setServices(userService, gameService, authService);
        otherHandler.handleDelete();
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
        UserData user = new UserData("username", "password", "email");
        GameData game = new GameData(1, "white", "black", "game1", new ChessGame());
        AuthData auth = new AuthData("username", "authToken");
        userAccess.addUser(user);
        gameAccess.addGame(game);
        authAccess.addAuth(auth);

        Assertions.assertEquals(new ClearResponse(null), otherHandler.handleDelete());
    }

    @Test
    @DisplayName("Register normal user")
    public void registerNormalUser() {
        RegisterRequest request = new RegisterRequest(normalUser);
        RegisterResponse response = otherHandler.register(request);

        Assertions.assertNull(response.message());
        Assertions.assertEquals(normalUser.username(), response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("Username already exists")
    public void registerUserExists() {
        RegisterRequest request = new RegisterRequest(normalUser);
        otherHandler.register(request);

        RegisterResponse response = otherHandler.register(request);

        Assertions.assertNotNull(response.message());
        Assertions.assertNull(response.username(), response.authToken());
        Assertions.assertEquals("Error: username already taken", response.message());
    }

    @Test
    @DisplayName("Register without username")
    public void registerWithoutUsername() {
        RegisterRequest request = new RegisterRequest(new UserData(null, "password", "email"));
        RegisterResponse response = otherHandler.register(request);

        Assertions.assertEquals("Error: bad request", response.message());
    }

    @Test
    @DisplayName("Normal login")
    public void normalLogin() {
        userAccess.addUser(normalUser);
        LoginRequest request = new LoginRequest(new UserData(normalUser.username(), normalUser.password(), null));
        LoginResponse response = otherHandler.login(request);

        Assertions.assertNull(response.message());
        Assertions.assertEquals("username", response.username());
        Assertions.assertNotNull(response.authToken());
    }
}
