package server;

import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import dataaccess.MemoryAuthAccess;
import service.*;
import spark.*;

public class Server {

    private final AuthHandler authHandler;
    private final SessionHandler sessionHandler;
    private final GameHandler gameHandler;
    private final MemoryUserAccess userAccess;
    private final MemoryGameAccess gameAccess;
    private final MemoryAuthAccess authAccess;
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    public Server() {
        authHandler = new AuthHandler();
        sessionHandler = new SessionHandler();
        gameHandler = new GameHandler();
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

        setVariables();

        Spark.delete("/db", authHandler::handleClear);

        Spark.post("/user", authHandler::handleRegister);

        Spark.post("/session", sessionHandler::login);

        Spark.delete("/session", sessionHandler::logout);

        Spark.get("/game", gameHandler::listGames);

        Spark.post("/game", gameHandler::createGame);

        Spark.put("/game", gameHandler::joinGame);

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
        userService.setAccess(userAccess, authAccess);
        gameService.setGameAccess(gameAccess, authAccess);
        authService.setAuthAccess(userAccess, gameAccess, authAccess);
        authHandler.setService(authService);
        sessionHandler.setService(userService);
        gameHandler.setService(gameService);
    }
}
