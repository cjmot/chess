package service;

import dataAccess.MemoryAuthAccess;
import dataAccess.MemoryUserAccess;
import model.LoginRequest;
import model.LoginResponse;
import model.LogoutRequest;
import model.LogoutResponse;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    private MemoryUserAccess userAccess;
    private MemoryAuthAccess authAccess;

    public UserService() {
        userAccess = null;
        authAccess = null;
    }

    public void setAccess(MemoryUserAccess userAccess, MemoryAuthAccess authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    public LoginResponse login(LoginRequest req) {
        UserData user = userAccess.getUser(req.user().username(), req.user().password());
        if (user == null) {
            return new LoginResponse(null, null, "Error: unauthorized");
        }

        String token = UUID.randomUUID().toString();
        String addedMessage = authAccess.addAuth(new AuthData(user.username(), token));
        if (addedMessage != null) {
            return new LoginResponse(null, null, addedMessage);
        }

        return new LoginResponse(user.username(), token, null);
    }

    public LogoutResponse logout(LogoutRequest req) {
        if (authAccess.getAuth(req.authToken()) == null) {
            return new LogoutResponse("Error: unauthorized");
        }
        String deletedMessage = authAccess.deleteAuth(req.authToken());
        if (deletedMessage != null) {
            return new LogoutResponse(deletedMessage);
        }

        return new LogoutResponse(null);
    }
}
