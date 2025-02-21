package service;

import chess.ChessGame;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Collection;

public class UserServiceTests {

    private static UserService userService;
    private static GameService gameService;
    private static AuthService authService;
    private static MemoryUserAccess userAccess;
    private static MemoryGameAccess gameAccess;
    private static MemoryAuthAccess authAccess;

    @BeforeAll
    public static void init() {
        userService = new UserService();
        gameService = new GameService();
        authService = new AuthService();
        userAccess = new MemoryUserAccess();
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        userService.setUserAccess(userAccess);
        gameService.setGameAccess(gameAccess);
        authService.setAuthAccess(authAccess);
    }

    @Test
    @DisplayName("Clear all data")
    public void clearAllData() {
        UserData user = new UserData("boogy", "down", "a lot");
        GameData game = new GameData(1, "white", "black", "game1", new ChessGame());
        AuthData auth = new AuthData("authToken", "username");
        userAccess.addUser(user);
        gameAccess.addGame(game);
        authAccess.addAuth(auth);

        userAccess.clear();
        gameAccess.clear();
        authAccess.clear();

        Assertions.assertEquals(0, userAccess.getUserData().size());
        Assertions.assertEquals(0, gameAccess.getGameData().size());
        Assertions.assertEquals(0, authAccess.getAuthData().size());
    }
}
