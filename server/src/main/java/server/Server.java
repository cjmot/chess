package server;

import dataaccess.MemoryDatabaseManager;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    private final AuthHandler authHandler;
    private final SessionHandler sessionHandler;
    private final GameHandler gameHandler;

    public Server() {
        MemoryDatabaseManager dbManager = new MemoryDatabaseManager();
        UserService userService = new UserService(dbManager);
        GameService gameService = new GameService(dbManager);
        AuthService authService = new AuthService(dbManager);
        authHandler = new AuthHandler(authService);
        sessionHandler = new SessionHandler(userService);
        gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

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
}
