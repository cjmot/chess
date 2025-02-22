package server;

import dto.ClearResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import model.AuthData;
import service.*;

public class OtherHandler {

    private AuthService authService;
    private GameService gameService;
    private UserService userService;

    public OtherHandler() {
        authService = null;
        gameService = null;
        userService = null;
    }

    public void setServices(UserService userService, GameService gameService, AuthService authService) {
        this.userService = userService;
        this.gameService = gameService;
        this.authService = authService;
    }

    public ClearResponse handleDelete() {
        userService.clearUserData();
        gameService.clearGameData();
        authService.clearAuthData();

        return new ClearResponse(null);
    }

    public RegisterResponse register(RegisterRequest req) {
        if (
                req.user() == null
                || req.user().username() == null
                || req.user().password() == null
                || req.user().email() == null
        ) {
            return new RegisterResponse(null, null, "Error: bad request");
        }
        if (userService.getUser(req.user().username()) != null) {
            return new RegisterResponse(null, null, "Error: already taken");
        }

        String message = userService.createUser(req.user());
        if (message != null) {
            return new RegisterResponse(null, null, "Error: " + message);
        }

        AuthData newAuth = authService.createAuth(req.user().username());

        return new RegisterResponse(newAuth.username(), newAuth.authToken(), null);
    }
}
