package server;

import com.google.gson.Gson;
import dto.*;
import model.AuthData;
import service.*;
import spark.Response;

public class OtherHandler {

    private AuthService authService;
    private GameService gameService;
    private UserService userService;
    private final Gson gson;

    public OtherHandler() {
        authService = null;
        gameService = null;
        userService = null;
        gson = new Gson();
    }

    public void setServices(UserService userService, GameService gameService, AuthService authService) {
        this.userService = userService;
        this.gameService = gameService;
        this.authService = authService;
    }

    public String handleClear(Response res) {
        ClearResponse response;

        ClearResponse userCleared = userService.clearUserData();
        ClearResponse gameCleared = gameService.clearGameData();
        ClearResponse authCleared = authService.clearAuthData();

        if (userCleared.message() != null) {
            res.status(500);
            response = userCleared;
        } else if (gameCleared.message() != null) {
            res.status(500);
            response = gameCleared;
        } else if (authCleared.message() != null) {
            res.status(500);
            response = authCleared;
        } else {
            res.status(200);
            response = userCleared;
        }
        res.type("application/json");

        return gson.toJson((response));
    }

    public RegisterResponse handleRegister(RegisterRequest req) {
        if (
                req.user() == null
                || req.user().username() == null
                || req.user().password() == null
                || req.user().email() == null
        ) {
            return new RegisterResponse(null, null, "Error: bad request");
        }
        if (userService.getUser(req.user().username()) != null) {
            return new RegisterResponse(null, null, "Error: username already taken");
        }

        String message = userService.createUser(req.user());
        if (message != null) {
            return new RegisterResponse(null, null, "Error: " + message);
        }

        AuthData newAuth = authService.createAuth(req.user().username());

        return new RegisterResponse(newAuth.username(), newAuth.authToken(), null);
    }
}
