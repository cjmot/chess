package dataaccess;

import model.AuthData;

public interface AuthAccess {
    String clear();

    String addAuth(AuthData newAuth);

    String deleteAuth(String token);

    AuthData getAuth(String token);
}
