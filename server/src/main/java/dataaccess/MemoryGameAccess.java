package dataaccess;

import model.GameData;

import java.util.HashSet;
import java.util.Set;

public class MemoryGameAccess implements GameAccess {

    private final Set<GameData> gameData;

    public MemoryGameAccess() {
        gameData = new HashSet<>();
    }

    public String clear() {
        gameData.clear();
        return null;
    }

    public Set<GameData> getAllGames() {
        return gameData;
    }

    public String addGame(GameData game) {
        if (!gameData.add(game)) {
            return "Failed to add game";
        }
        return null;
    }

    public String updateGame(String playerColor, Integer gameID, String username) {
        for (GameData game : gameData) {
            if (game.gameID().equals(gameID)) {
                if (playerColor.equals("WHITE")) {
                    return setWhite(game, username);
                } else {
                    return setBlack(game, username);
                }
            }
        }
        return "Failed to update game";
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
