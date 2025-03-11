package dataaccess.sql;

import exception.ResponseException;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

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

    public AuthData getAuth(String token) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username, auth_token FROM auth WHERE auth_token=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        String storedToken = rs.getString("auth_token");
                        if (Objects.equals(storedToken, token)) {
                            return new AuthData(username, token);

                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }
}
