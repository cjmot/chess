package service;

import dataaccess.MemoryUserAccess;

public class UserService {

    private MemoryUserAccess userAccess;

    public UserService() {
        userAccess = null;
    }

    public void clearUserData() {
        setUserAccess(userAccess);
        this.userAccess.clear();
    }

    public void setUserAccess(MemoryUserAccess userAccess) {
        this.userAccess = userAccess;
    }
}
