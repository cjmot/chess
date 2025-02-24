package dataAccess;

import model.UserData;

import java.util.HashSet;
import java.util.Set;

public class MemoryUserAccess implements UserAccess {

    private final Set<UserData> userData;

    public MemoryUserAccess() {
        userData = new HashSet<>();
    }

    public String clear() {
        userData.clear();
        return null;
    }

    public UserData getUserByUsername(String username) {
        for (UserData user : userData) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public UserData getUser(String username, String password) {
        for (UserData user : userData) {
            if (user.username().equals(username) && user.password().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public Set<UserData> getAllUsers() {
        return userData;
    }

    public String addUser(UserData user) {
        if (!userData.add(user)) {
            return "Failed to add user";
        }
        return null;
    }
}
