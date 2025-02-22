package dataaccess;
import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryAuthAccess implements UserAccess {

    private Collection<AuthData> authData;

    public MemoryAuthAccess() {
        authData = new ArrayList<>();
    }

    public void clear() {
        authData.clear();
    }

    public AuthData getAuthByUsername(String username) {
        for (AuthData auth : authData) {
            if (auth.username().equals(username)){
                return auth;
            }
        }
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

    public AuthData addAuth(AuthData newAuth) {
        if (!authData.add(newAuth)) {
            return null;
        }
        return newAuth;
    }
}
