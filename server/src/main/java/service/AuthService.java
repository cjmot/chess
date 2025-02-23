package service;

import dataaccess.*;
import model.AuthData;

import java.util.UUID;

public class AuthService {

    private MemoryAuthAccess authAccess;

    public AuthService() {
        authAccess = null;
    }

    public void clearAuthData() {
        authAccess.clear();
    }

    public void setAuthAccess(MemoryAuthAccess authAccess) {
        this.authAccess = authAccess;
    }

    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        return authAccess.addAuth(new AuthData(username, authToken));
    }

    public String deleteAuth(String auth) {
        return authAccess.deleteAuth(auth);
    }

    public boolean verifyAuth(String token) {
        return authAccess.getAuth(token);
    }
}
