package dataaccess;
import model.AuthData;

import java.util.HashSet;
import java.util.Set;

public class MemoryAuthAccess implements UserAccess {

    private Set<AuthData> authData;

    public MemoryAuthAccess() {
        authData = new HashSet<>();
    }

    public String clear() {
        authData.clear();
        return null;
    }

    public Set<AuthData> getAllAuth() {
        return authData;
    }

    public String addAuth(AuthData newAuth) {
        if (!authData.add(newAuth)) {
            return "Failed to add auth";
        }
        return null;
    }

    public String deleteAuth(String token) {
        AuthData authToRemove = null;
        for (AuthData auth : authData) {
            if (auth.authToken().equals(token)) {
                authToRemove = auth;
            }
        }

        if (!authData.remove(authToRemove)) {
            return "Failed to remove auth";
        }
        return null;
    }

    public boolean getAuth(String token) {
        for (AuthData auth : authData) {
            if (auth.authToken().equals(token)) {
                return true;
            }
        }
        return false;
    }
}
