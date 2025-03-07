package dataaccess;

import exception.ResponseException;
import model.GameData;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.Types.NULL;

public class SqlGameAccess {

    public SqlGameAccess() throws ResponseException {
        configureDatabase();
    }

    public String clear() {
        try {
            executeUpdate("TRUNCATE TABLE game");
            return null;
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    private void configureDatabase() throws ResponseException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            String createStatement = """
                    CREATE TABLE IF NOT EXISTS game (
                      game_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      white_username varchar(256),
                      black_username varchar(256),
                      game_name varchar(256) UNIQUE,
                      game JSON NOT NULL
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
