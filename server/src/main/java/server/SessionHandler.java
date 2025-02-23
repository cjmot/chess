package server;

import dto.LoginRequest;
import dto.LoginResponse;
import dto.LogoutRequest;
import dto.LogoutResponse;
import model.AuthData;
import service.AuthService;
import service.UserService;

public class SessionHandler {

    private AuthService authService;
    private UserService userService;

    public SessionHandler() {
        authService = null;
        userService = null;
    }

    public void setServices(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    public LoginResponse login(LoginRequest req) {
        if (userService.getUser(req.user().username(), req.user().password()) == null) {
            return new LoginResponse(null, null, "Error: unauthorized");
        }

        AuthData newAuth = authService.createAuth(req.user().username());
        return new LoginResponse(newAuth.username(), newAuth.authToken(), null);
    }

    public LogoutResponse logout(LogoutRequest req) {
        if (!authService.verifyAuth(req.authToken())) {
            return new LogoutResponse("Error: unauthorized");
        }
        return new LogoutResponse(authService.deleteAuth(req.authToken()));
    }
}
