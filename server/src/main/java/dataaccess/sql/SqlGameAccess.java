package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dto.ListGamesResponse;
import exception.ResponseException;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class SqlGameAccess {

    private final Gson gson;

    public SqlGameAccess() {
        gson = new Gson();
        try {
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
        } catch (ResponseException e) {
            System.err.println(e.getMessage());
        }
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
        String chessGameJson = gson.toJson(game.game());
        String statement = "INSERT INTO game (white_username, black_username, game_name, game) VALUES (?, ?, ?, ?)";
        try {
            int gameID = SqlDatabaseManager.executeUpdate(
                    statement, game.whiteUsername(), game.blackUsername(), game.gameName(), chessGameJson
            );
            if (gameID == 0) {
                return "No auto-generated keys made";
            }
            return String.valueOf(gameID);
        } catch (ResponseException e) {
            return String.format("Failed to add game: %s", e.getMessage());
        }

    }

    public ListGamesResponse getAllGames() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT game_id, white_username, black_username, game_name, game FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    Collection<GameData> games = new ArrayList<>();
                    while (rs.next()) {
                        games.add(getGameFromRs(rs));
                    }
                    return new ListGamesResponse(games, null);
                }
            }
        } catch (Exception e) {
            return new ListGamesResponse(null, String.format("Failed to get games: %s", e.getMessage()));
        }
    }

    public String updateGame(String playerColor, Integer gameID, String username) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT white_username, black_username FROM game WHERE game_id=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String whiteUsername = rs.getString("white_username");
                        String blackUsername = rs.getString("black_username");
                        if (Objects.equals(playerColor, "WHITE") && whiteUsername == null) {
                            return setDbUsername(username, "WHITE", gameID);
                        } else if (Objects.equals(playerColor, "BLACK") && blackUsername == null) {
                            return setDbUsername(username, "BLACK", gameID);
                        } else {
                            return "Error: Color already taken";
                        }
                    }
                }
            }
        } catch (Exception e) {
            return String.format("Failed to update game: %s", e.getMessage());
        }
        return "Failed to update game";
    }

    private String setDbUsername(String username, String color, Integer gameID) throws ResponseException {
        String statement;
        if (Objects.equals(color, "WHITE")) {
            statement = "UPDATE game SET white_username=? WHERE game_id=?";
        } else {
            statement = "UPDATE game SET black_username=? WHERE game_id=?";
        }
        SqlDatabaseManager.executeUpdate(statement, username, gameID);
        return null;
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
