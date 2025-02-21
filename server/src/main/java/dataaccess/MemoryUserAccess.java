package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryUserAccess implements UserAccess {

    private Collection<UserData> userData;

    public MemoryUserAccess() {
        userData = new ArrayList<>();
    }

    public void clear() {
        userData.clear();
    }

    public Collection<UserData> getUserData() {
        return userData;
    }

    public boolean addUser(UserData user) {
        return userData.add(user);
    }
}
