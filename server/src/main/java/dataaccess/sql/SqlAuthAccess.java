package dataaccess.sql;

import exception.ResponseException;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class SqlAuthAccess {

    public SqlAuthAccess() {
        try {
            String createStatement = """
                    CREATE TABLE IF NOT EXISTS auth (
                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username varchar(256) NOT NULL,
                      auth_token varchar(256) UNIQUE NOT NULL
                    );
                    """;
            SqlDatabaseManager.configureDatabase(createStatement);
        } catch (ResponseException e) {
            System.err.println(e.getMessage());
        }
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

    public AuthData getAuth(String token) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username, auth_token FROM auth WHERE auth_token=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    return getAndCheckAuthFromRs(rs, token);
                }
            }
        } catch (Exception e) {
            return new AuthData(null, null, String.format("Failed to get auth: %s", e.getMessage()));
        }
    }

    public String deleteAuth(String token) {
        try {
            String statement = "DELETE FROM auth WHERE auth_token=?";
            SqlDatabaseManager.executeUpdate(statement, token);
            return null;
        } catch (ResponseException e) {
            return String.format("Failed to remove auth: %s", e.getMessage());
        }
    }

    private AuthData getAndCheckAuthFromRs(ResultSet rs, String token) throws SQLException {
        if (rs.next()) {
            String username = rs.getString("username");
            String storedToken = rs.getString("auth_token");
            if (Objects.equals(storedToken, token)) {
                return new AuthData(username, token, null);
            }
        }
        return new AuthData(null, null, "Error: Wrong Auth Token");
    }
}
