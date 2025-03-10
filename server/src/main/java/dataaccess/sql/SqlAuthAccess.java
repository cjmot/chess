package dataaccess.sql;

import exception.ResponseException;
import model.AuthData;

public class SqlAuthAccess {

    public SqlAuthAccess() throws ResponseException {
        String createStatement = """
                    CREATE TABLE IF NOT EXISTS auth (
                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username varchar(256) NOT NULL,
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

    public String addAuth(AuthData newAuth) {
        try {
            String statement = "INSERT INTO auth (username, auth_token) VALUES (?, ?)";
            SqlDatabaseManager.executeUpdate(statement, newAuth.username(), newAuth.authToken());
            return null;
        } catch (ResponseException e) {
            return String.format("Failed to add auth: %s", e.getMessage());
        }
    }
}
