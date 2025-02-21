package service;

import dataaccess.*;

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
}
