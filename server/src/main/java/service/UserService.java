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

    public UserData getUser(String username) {
        return userAccess.getUserByUsername(username);
    }

    public String createUser(UserData user) {
        return userAccess.addUser(user);
    }
}
