package service;

import dataaccess.MemoryDatabaseManager;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryUserAccess;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.LogoutRequest;
import dto.LogoutResponse;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final MemoryUserAccess userAccess;
    private final MemoryAuthAccess authAccess;

    public UserService(MemoryDatabaseManager dbManager) {
        userAccess = dbManager.userAccess();
        authAccess = dbManager.authAccess();
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
