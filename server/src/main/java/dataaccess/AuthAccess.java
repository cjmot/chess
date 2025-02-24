package dataaccess;

import model.AuthData;

import java.util.Set;

public interface AuthAccess {
    String clear();

    Set<AuthData> getAllAuth();

    String addAuth(AuthData newAuth);

    String deleteAuth(String token);

    AuthData getAuth(String token);
}
