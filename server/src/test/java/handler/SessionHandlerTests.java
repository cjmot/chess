package handler;

import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryUserAccess;
import dto.*;
import model.UserData;
import org.junit.jupiter.api.*;
import server.OtherHandler;
import server.SessionHandler;
import service.AuthService;
import service.UserService;

public class SessionHandlerTests {

    private static SessionHandler sessionHandler;
    private static OtherHandler otherHandler;
    private static UserService userService;
    private static AuthService authService;
    private static MemoryUserAccess userAccess;
    private static MemoryAuthAccess authAccess;
    private static UserData normalUser = new UserData("username", "password", "email");

    @BeforeAll
    public static void init() {
        sessionHandler = new SessionHandler();
        otherHandler = new OtherHandler();
        userService = new UserService();
        authService = new AuthService();
        userAccess = new MemoryUserAccess();
        authAccess = new MemoryAuthAccess();
        userService.setUserAccess(userAccess);
        authService.setAuthAccess(authAccess);
        sessionHandler.setServices(userService, authService);
        otherHandler.setServices(userService, null, authService);
    }

    @AfterEach
    public void close() {
        userAccess.clear();
        authAccess.clear();
    }

    @Test
    @DisplayName("Normal login")
    public void normalLogin() {
        userAccess.addUser(normalUser);
        LoginRequest request = new LoginRequest(new UserData(normalUser.username(), normalUser.password(), null));
        LoginResponse response = sessionHandler.login(request);

        Assertions.assertNull(response.message());
        Assertions.assertEquals("username", response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("Login unauthorized")
    public void loginUnauthorized() {
        LoginRequest request = new LoginRequest(normalUser);
        LoginResponse response = sessionHandler.login(request);

        Assertions.assertNull(response.username(), response.authToken());
        Assertions.assertEquals("Error: unauthorized", response.message());
    }

    @Test
    @DisplayName("Normal logout")
    public void normalLogout() {
        String token = otherHandler.handleRegister(new RegisterRequest(normalUser)).authToken();
        LogoutResponse response = sessionHandler.logout(new LogoutRequest(token));

        Assertions.assertNull(response.message());
        Assertions.assertTrue(authAccess.getAuthData().isEmpty());
    }

    @Test
    @DisplayName("Logout unauthorized")
    public void logoutUnauthorized() {
        otherHandler.handleRegister(new RegisterRequest(normalUser));
        LogoutResponse response = sessionHandler.logout(new LogoutRequest("wrong token"));

        Assertions.assertEquals("Error: unauthorized", response.message());
        Assertions.assertEquals(1, authAccess.getAuthData().size());
    }
}
