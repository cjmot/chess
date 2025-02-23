package service;

import dataaccess.MemoryUserAccess;
import dto.ClearResponse;
import model.UserData;

public class UserService {

    private MemoryUserAccess userAccess;

    public UserService() {
        userAccess = null;
    }

    public ClearResponse clearUserData() {
        return new ClearResponse(userAccess.clear());
    }

    public void setUserAccess(MemoryUserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public UserData getUser(String username) {
        return userAccess.getUser(username);
    }

    public UserData getUser(String username, String password) {
        return userAccess.getUser(username, password);
    }


    public String createUser(UserData user) {
        return userAccess.addUser(user);
    }
}
