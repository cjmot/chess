package client;

import dto.RegisterRequest;
import dto.RegisterResponse;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static final UserData normalUser = new UserData("username", "password", "email");

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
        RegisterRequest request = new RegisterRequest("username", "password", "email");

        RegisterResponse response = serverFacade.register(request);

        Assertions.assertEquals("username", response.username());
    }

    @Test
    @DisplayName("Username Already Taken")
    public void usernameTakenTest() throws ResponseException {
        RegisterRequest request = new RegisterRequest("username", "password", "email");

        serverFacade.register(request);

        Assertions.assertThrows(ResponseException.class, () -> serverFacade.register(request));
    }
}
