package service;

import dataaccess.sql.SqlAuthAccess;
import dataaccess.sql.SqlDatabaseManager;
import dataaccess.sql.SqlUserAccess;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.LogoutRequest;
import dto.LogoutResponse;
import model.AuthData;

import java.util.UUID;

public class UserService {

    private final SqlUserAccess userAccess;
    private final SqlAuthAccess authAccess;

    public UserService(SqlDatabaseManager dbManager) {
        userAccess = dbManager.userAccess();
        authAccess = dbManager.authAccess();
    }

    public LoginResponse login(LoginRequest req) {
        LoginResponse userResponse = userAccess.getUser(req.user().username(), req.user().password());
        if (userResponse.message() != null) {
            return new LoginResponse(null, null, userResponse.message());
        }

        String token = UUID.randomUUID().toString();
        String addedMessage = authAccess.addAuth(new AuthData(userResponse.username(), token, null));
        if (addedMessage != null) {
            return new LoginResponse(null, null, addedMessage);
        }

        return new LoginResponse(userResponse.username(), token, null);
    }

    public LogoutResponse logout(LogoutRequest req) {
        AuthData authResult = authAccess.getAuth(req.authToken());
        if (authResult.message() != null) {
            return new LogoutResponse("Error: unauthorized");
        }
        String deletedMessage = authAccess.deleteAuth(req.authToken());
        if (deletedMessage != null) {
            return new LogoutResponse(deletedMessage);
        }

        return new LogoutResponse(null);
    }
}
