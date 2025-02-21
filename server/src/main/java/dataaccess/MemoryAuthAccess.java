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

    public Collection<AuthData> getAuthData() {
        return authData;
    }

    public boolean addAuth(AuthData auth) {
        return authData.add(auth);
    }
}
