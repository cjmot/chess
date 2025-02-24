package dataaccess;
import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryAuthAccess implements UserAccess {

    private Collection<AuthData> authData;

    public MemoryAuthAccess() {
        authData = new ArrayList<>();
    }

    public String clear() {
        authData.clear();
        return null;
    }

    public AuthData getAuthByToken(String token) {
        for (AuthData auth : authData) {
            if (auth.authToken().equals(token)){
                return auth;
            }
        }
        return null;
    }

    public Collection<AuthData> getAuthData() {
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
        return authData.stream().anyMatch(auth -> auth.authToken().equals(token));
    }
}
