package service;

import dataaccess.MemoryUserAccess;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryAuthAccess;
import dataaccess.DatabaseManager;
import dto.ClearResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import model.AuthData;

import java.util.UUID;

public class AuthService {

    private final MemoryUserAccess userAccess;
    private final MemoryGameAccess gameAccess;
    private final MemoryAuthAccess authAccess;

    public AuthService(DatabaseManager dbManager) {
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
        if (userAccess.getUserByUsername(req.user().username()) != null) {
            return new RegisterResponse(null, null, "Error: already taken");
        }

        String addedMessage = userAccess.addUser(req.user());
        if (addedMessage != null) {
            return new RegisterResponse(null, null, addedMessage);
        }

        AuthData newAuth = new AuthData(req.user().username(), UUID.randomUUID().toString());
        addedMessage = authAccess.addAuth(newAuth);
        if (addedMessage != null) {
            return new RegisterResponse(null, null, addedMessage);
        }

        return new RegisterResponse(newAuth.username(), newAuth.authToken(), null);
    }
}
