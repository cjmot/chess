package dataaccess;

import model.UserData;

public interface UserAccess {

    String clear();

    UserData getUserByUsername(String username);

    UserData getUser(String username, String password);

    String addUser(UserData user);
}
