package dataaccess;

import exception.ResponseException;

public class SqlDatabaseManager {

    private final SqlUserAccess userAccess;
    private final SqlGameAccess gameAccess;
    private final SqlAuthAccess authAccess;

    public SqlDatabaseManager() throws ResponseException {
            userAccess = new SqlUserAccess();
            gameAccess = new SqlGameAccess();
            authAccess = new SqlAuthAccess();
    }

    public SqlUserAccess userAccess() {
        return this.userAccess;
    }

    public SqlGameAccess gameAccess() {
        return this.gameAccess;
    }

    public SqlAuthAccess authAccess() {
        return this.authAccess;
    }
}