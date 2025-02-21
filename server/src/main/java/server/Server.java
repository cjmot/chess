package server;

import com.google.gson.Gson;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import dataaccess.MemoryAuthAccess;
import service.*;
import spark.*;

public class Server {

    private final OtherHandler otherHandler;
    private final MemoryUserAccess userAccess;
    private final MemoryGameAccess gameAccess;
    private final MemoryAuthAccess authAccess;
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    public Server() {
        otherHandler = new OtherHandler();
        userAccess = new MemoryUserAccess();
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        userService = new UserService();
        gameService = new GameService();
        authService = new AuthService();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Gson gson = new Gson();

        setVariables();

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (_, res) ->
            gson.toJson(otherHandler.handleDelete(res))
        );

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void setVariables() {
        userService.setUserAccess(userAccess);
        gameService.setGameAccess(gameAccess);
        authService.setAuthAccess(authAccess);
        otherHandler.setServices(userService, gameService, authService);
    }
}
