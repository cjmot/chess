package service;

import dataaccess.sql.SqlAuthAccess;
import dataaccess.sql.SqlDatabaseManager;
import dataaccess.sql.SqlGameAccess;
import dataaccess.sql.SqlUserAccess;
import dto.ClearResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class AuthService {

    private final SqlUserAccess userAccess;
    private final SqlGameAccess gameAccess;
    private final SqlAuthAccess authAccess;

    public AuthService(SqlDatabaseManager dbManager) {
        userAccess = dbManager.userAccess();
        gameAccess = dbManager.gameAccess();
        authAccess = dbManager.authAccess();
    }

    public ClearResponse clear() {
        ClearResponse response = new ClearResponse(null);

        String userMessage = userAccess.clear();
        String gameMessage = gameAccess.clear();
        String authMessage = authAccess.clear();

        if (userMessage != null) {
            response = new ClearResponse(userMessage);
        } else if (gameMessage != null) {
            response = new ClearResponse(gameMessage);
        } else if (authMessage != null) {
            response = new ClearResponse(authMessage);
        }
        return response;
    }

    public RegisterResponse register(RegisterRequest req) {
        if (userAccess.getUserByUsername(req.username()) != null) {
            return new RegisterResponse(null, null, "Error: already taken");
        }

        String addedMessage = userAccess.addUser(new UserData(req.username(), req.password(), req.email()));
        if (addedMessage != null) {
            return new RegisterResponse(null, null, addedMessage);
        }

        AuthData newAuth = new AuthData(req.username(), UUID.randomUUID().toString(), null);
        addedMessage = authAccess.addAuth(newAuth);
        if (addedMessage != null) {
            return new RegisterResponse(null, null, addedMessage);
        }

        return new RegisterResponse(newAuth.username(), newAuth.authToken(), null);
    }

    public AuthData verifyAuth(String authToken) {
        AuthData response = authAccess.getAuth(authToken);
        if (response.message() != null) {
            return new AuthData(null, null, response.message());
        }
        return response;
    }
}
