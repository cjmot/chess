package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameAccess {

    private final Gson gson;

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
        gson = new Gson();
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

    public Collection<GameData> getAllGames() throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "Select game_id, white_username, black_username, game_name, game FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    Collection<GameData> games = new ArrayList<>();
                    while (rs.next()) {
                        games.add(getGameFromRs(rs));
                    }
                    return games;
                }
            }
        } catch (Exception e) {
            throw new ResponseException(String.format("Failed to get games: %s", e.getMessage()));
        }
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

    private GameData getGameFromRs(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("game_id");
        String whiteUsername = rs.getString("white_username");
        String blackUsername = rs.getString("black_username");
        String gameName = rs.getString("game_name");
        ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
