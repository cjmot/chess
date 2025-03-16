package client;

import dto.*;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private final RegisterRequest normalRegisterReq = new RegisterRequest("username", "password", "email");
    private final LoginRequest normalLoginReq = new LoginRequest("username", "password");

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDb() throws ResponseException {
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @DisplayName("Clear Db Test")
    public void clearDbTest() {
        Assertions.assertDoesNotThrow(serverFacade::clear);
    }

    @Test
    @DisplayName("Normal Register Test")
    public void normalRegisterTest() throws ResponseException {
        RegisterResponse response = serverFacade.register(normalRegisterReq);

        Assertions.assertEquals("username", response.username());
    }

    @Test
    @DisplayName("Username Already Taken")
    public void usernameTakenTest() throws ResponseException {
        serverFacade.register(normalRegisterReq);

        Assertions.assertThrows(ResponseException.class, () -> serverFacade.register(normalRegisterReq));
    }

    @Test
    @DisplayName("Normal Login Test")
    public void normalLoginTest() throws ResponseException {
        serverFacade.register(normalRegisterReq);

        LoginResponse response = serverFacade.login(normalLoginReq);

        Assertions.assertEquals("username", response.username());
    }

    @Test
    @DisplayName("Wrong Password login")
    public void wrongPasswordLoginTest() throws ResponseException {
        serverFacade.register(normalRegisterReq);

        LoginRequest wrongRequest = new LoginRequest("username", "wrongPassword");

        Assertions.assertThrows(ResponseException.class, () -> serverFacade.login(wrongRequest));
    }

    @Test
    @DisplayName("Normal Logout Test")
    public void normalLogoutTest() throws ResponseException {
        serverFacade.register(normalRegisterReq);
        String token = serverFacade.login(normalLoginReq).authToken();

        Assertions.assertDoesNotThrow(() -> serverFacade.logout(new LogoutRequest(token)));
    }

    @Test
    @DisplayName("Wrong Authentication Logout")
    public void wrongAuthLogoutTest() throws ResponseException {
        normalLogin();

        Assertions.assertThrows(
                ResponseException.class,
                () -> serverFacade.logout(new LogoutRequest("wrong token"))
        );
    }

    @Test
    @DisplayName("Normal Create Game")
    public void normalCreateGameTest() throws ResponseException {
        String token = normalLogin();

        CreateGameResponse response = serverFacade.createGame(new CreateGameRequest("game1", token));
        Assertions.assertEquals(1, response.gameID());
    }

    @Test
    @DisplayName("Bad Auth Create Game")
    public void badAuthCreateGameTest() throws ResponseException {
        normalLogin();

        Assertions.assertThrows(
                ResponseException.class,
                () -> serverFacade.createGame(new CreateGameRequest("game1", "wrong token"))
        );
    }

    private String normalLogin() throws ResponseException {
        serverFacade.register(normalRegisterReq);
        return serverFacade.login(normalLoginReq).authToken();
    }
}
