package dataaccess.sql;

import exception.ResponseException;
import model.GameData;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.Types.NULL;

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

    public void clearAll() {
        userAccess.clear();
        gameAccess.clear();
        authAccess.clear();
    }

    static void configureDatabase(String createStatement) throws ResponseException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(createStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    static void executeUpdate(String statement, Object... params) throws ResponseException {
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