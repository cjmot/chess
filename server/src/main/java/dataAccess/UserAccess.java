package dataAccess;

import model.UserData;

import java.util.Set;

public interface UserAccess {

    String clear();

    UserData getUserByUsername(String username);

    UserData getUser(String username, String password);

    Set<UserData> getAllUsers();

    String addUser(UserData user);
}
