package dataaccess.sql;

import exception.ResponseException;

public class SqlGameAccess {

    public SqlGameAccess() throws ResponseException {
        String createStatement = """
                CREATE TABLE IF NOT EXISTS game (
                  game_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                  white_username varchar(256),
                  black_username varchar(256),
                  game_name varchar(256) UNIQUE,
                  game JSON NOT NULL
                );
                """;
        SqlDatabaseManager.configureDatabase(createStatement);
    }

    public String clear() {
        try {
            SqlDatabaseManager.executeUpdate("TRUNCATE TABLE game");
            return null;
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }
}
