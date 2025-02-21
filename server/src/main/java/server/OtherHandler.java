package server;

import dto.ClearResponse;
import spark.Response;
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

    public ClearResponse handleDelete(Response res) {
        userService.clearUserData();
        gameService.clearGameData();
        authService.clearAuthData();

        res.status(200);
        res.type("application/json");
        return new ClearResponse(null);
    }
}
