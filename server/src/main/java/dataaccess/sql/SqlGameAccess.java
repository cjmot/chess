package dataaccess.sql;

import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;

import java.util.Collection;

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

    public String addGame(GameData game) {
        try {
            Gson gson = new Gson();
            String chessGameJson = gson.toJson(game.game());
            String statement = "INSERT INTO game (white_username, black_username, game_name, game) VALUES (?, ?, ?, ?)";
            SqlDatabaseManager.executeUpdate(
                    statement,
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    chessGameJson
            );
            return null;
        } catch (ResponseException e) {
            return String.format("Failed to add game: %s", e.getMessage());
        }
    }

    public Collection<GameData> getAllGames() {
        throw new RuntimeException("Not implemented");
    }

    public String updateGame(String playerColor, Integer gameID, String username) {
        throw new RuntimeException("Not implemented");
    }

    private String setWhite(GameData game, String username) {
        if (game.whiteUsername() != null) {
            return "Error: already taken";
        } else {
            game.setWhiteUsername(username);
            return null;
        }
    }

    private String setBlack(GameData game, String username) {
        if (game.blackUsername() != null) {
            return "Error: already taken";
        } else {
            game.setBlackUsername(username);
            return null;
        }
    }
}
