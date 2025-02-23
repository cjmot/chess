package server;

import com.google.gson.Gson;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import dataaccess.MemoryAuthAccess;
import dto.*;
import model.UserData;
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
                {
                    ClearResponse response = otherHandler.handleDelete();
                    if (response.message() != null) {
                        res.status(500);
                    } else {
                        res.status(200);
                    }
                    res.type("application/json");
                    return gson.toJson(response);
                }
        );

        Spark.post("/user", (req, res) -> {
            UserData user = gson.fromJson(req.body(), UserData.class);
            RegisterResponse response = otherHandler.register(new RegisterRequest(user));
            if (response.message() != null) {
                if (response.message().contains("bad request")) {
                    res.status(400);
                } else if (response.message().contains("already taken")) {
                    res.status(403);
                } else res.status(500);
            } else {
                res.status(200);
            }
            res.type("application/json");
            return gson.toJson(response);
        });

        Spark.post("/session", (req, res) -> {
            UserData user = gson.fromJson(req.body(), UserData.class);
            LoginResponse response = otherHandler.login(new LoginRequest(user));
            if (response.message() != null) {
                if (response.message().contains("unauthorized")) {
                    res.status(401);
                }
                else res.status(500);
            } else {
                res.status(200);
            }
            res.type("application/json");
            return gson.toJson(response);
        });

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
