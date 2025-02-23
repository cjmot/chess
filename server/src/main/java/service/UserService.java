package service;

import dataaccess.MemoryUserAccess;
import model.UserData;

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

    public UserData getUserByUsername(String username) {
        return userAccess.getUserByUsername(username);
    }

    public UserData getUserByCreds(String username, String password) {
        return userAccess.getUserByCreds(username, password);
    }

    public String createUser(UserData user) {
        return userAccess.addUser(user);
    }
}
