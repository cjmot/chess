package dataaccess.sql;

import dto.LoginResponse;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Objects;

public class SqlUserAccess {

    public SqlUserAccess() {
        try {
            String createStatement = """
                    CREATE TABLE IF NOT EXISTS user (
                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username varchar(256) UNIQUE NOT NULL,
                      password varchar(256) UNIQUE NOT NULL,
                      email varchar(256) UNIQUE NOT NULL
                    );
                    """;
            SqlDatabaseManager.configureDatabase(createStatement);
        } catch (ResponseException e) {
            System.err.println(e.getMessage());
        }
    }

    public String clear() {
        try {
            SqlDatabaseManager.executeUpdate("TRUNCATE TABLE user;");
            return null;
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String addUser(UserData user) {
        try {
            String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            SqlDatabaseManager.executeUpdate(statement, user.username(), hashedPassword, user.email());
            return null;
        } catch (ResponseException e) {
            if (e.getMessage().contains("Duplicate entry 'username'")) {
                return "Failed to add user: username already exists";
            } else if (e.getMessage().contains("Duplicate entry 'email'")) {
                return "Failed to add user: email already exists";
            } else {
                return e.getMessage();
            }
        }
    }

    public AuthData getUserByUsername(String username) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username FROM user WHERE username=?;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(username, null, null);
                    }
                }
            }
        } catch (Exception e) {
            return new AuthData(null, null, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public LoginResponse getUser(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username, password FROM user WHERE username=?;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && readUser(rs, username).message() == null) {
                        return checkPassword(rs, username, password);
                     }
                }
            }
        } catch (Exception e) {
            return new LoginResponse(
                    null, null, String.format("Unable to read data: %s", e.getMessage())
            );
        }
        return new LoginResponse(null, null, "Error: unauthorized");
    }

    private AuthData readUser(ResultSet rs, String username) throws SQLException {
        if (Objects.equals(rs.getString("username"), username)) {
            return new AuthData(username, null, null);
        } else {
            return new AuthData(null, null, "Error: unauthorized");
        }
    }
    private LoginResponse checkPassword(ResultSet rs, String username, String password) throws SQLException {
        String hashedPassword = rs.getString("password");
        if (BCrypt.checkpw(password, hashedPassword)) {
            return new LoginResponse(username, null, null);
        } else {
            return new LoginResponse(null, null, "Error: unauthorized");
        }
    }
}
