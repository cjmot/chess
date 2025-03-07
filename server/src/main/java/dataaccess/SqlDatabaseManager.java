package dataaccess;

import exception.ResponseException;

public class SqlDatabaseManager {

    private final SqlUserAccess userAccess;

    public SqlDatabaseManager() throws ResponseException {
            userAccess = new SqlUserAccess();
    }

    public SqlUserAccess userAccess() {
        return this.userAccess;
    }
}