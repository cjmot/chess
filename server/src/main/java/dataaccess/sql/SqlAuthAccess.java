package dataaccess.sql;

import exception.ResponseException;

public class SqlAuthAccess {

    public SqlAuthAccess() throws ResponseException {
        String createStatement = """
                    CREATE TABLE IF NOT EXISTS auth (
                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username varchar(256) UNIQUE NOT NULL,
                      auth_token varchar(256) UNIQUE NOT NULL
                    );
                    """;
        SqlDatabaseManager.configureDatabase(createStatement);
    }

    public String clear() {
        try {
            SqlDatabaseManager.executeUpdate("TRUNCATE TABLE auth");
            return null;
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }
}
