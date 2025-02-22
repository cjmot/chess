package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashSet;

public class MemoryUserAccess implements UserAccess {

    private Collection<UserData> userData;

    public MemoryUserAccess() {
        userData = new HashSet<>();
    }

    public void clear() {
        userData.clear();
    }

    public UserData getUserByUsername(String username) {
        for (UserData user : userData) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public Collection<UserData> getAllUsers() {
        return userData;
    }

    public String addUser(UserData user) {
        if (!userData.add(user)) {
            return "Failed to add user";
        }
        return null;
    }
}
