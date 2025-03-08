package dataaccess;

import exception.ResponseException;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;

import static java.sql.Types.NULL;

public class SqlUserAccess {

    public SqlUserAccess() throws ResponseException {
        configureDatabase();
    }

    public String clear() {
        try {
            executeUpdate("TRUNCATE TABLE user;");
            return null;
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String addUser(UserData user) {
        try {
            var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
            var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            executeUpdate(statement, user.username(), hashedPassword, user.email());
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
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "SELECT username FROM user WHERE username=?;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
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

    private String readUser(ResultSet rs) throws SQLException {
        return rs.getString("username");
    }

    private void configureDatabase() throws ResponseException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            String createStatement = """
                    CREATE TABLE IF NOT EXISTS user (
                      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username varchar(256) UNIQUE NOT NULL,
                      password varchar(256) UNIQUE NOT NULL,
                      email varchar(256) UNIQUE NOT NULL
                    );
                    """;
            try (var preparedStatement = conn.prepareStatement(createStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private void executeUpdate(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case GameData p -> ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);
                        default -> throw new IllegalStateException("Unexpected value: " + param);
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }

            }
        } catch (SQLException e) {
            throw new ResponseException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
