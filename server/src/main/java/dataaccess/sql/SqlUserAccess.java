package dataaccess.sql;

import exception.ResponseException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Objects;

public class SqlUserAccess {

    public SqlUserAccess() throws ResponseException {
        String createStatement = """
                    CREATE TABLE IF NOT EXISTS user (
                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username varchar(256) UNIQUE NOT NULL,
                      password varchar(256) UNIQUE NOT NULL,
                      email varchar(256) UNIQUE NOT NULL
                    );
                    """;
        SqlDatabaseManager.configureDatabase(createStatement);
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

    public String getUserByUsername(String username) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username FROM user WHERE username=?;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (Objects.equals(readUser(rs), username)) {
                            return username;
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public String getUser(String username, String password) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username, password FROM user WHERE username=?;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && Objects.equals(readUser(rs), username)) {
                        String hashedPassword = rs.getString("password");
                        if (BCrypt.checkpw(password, hashedPassword)) {
                            return username;
                        }
                     }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    private String readUser(ResultSet rs) throws SQLException {
        return rs.getString("username");
    }
}
