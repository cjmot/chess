package dataaccess;

import model.GameData;

import java.util.HashSet;
import java.util.Set;

public class MemoryGameAccess implements UserAccess {

    private Set<GameData> gameData;

    public MemoryGameAccess() {
        gameData = new HashSet<>();
    }

    public String clear() {
        gameData.clear();
        return null;
    }

    public GameData getGameByGameName(String name) {
        for (GameData game : gameData) {
            if (game.gameName().equals(name)) {
                return game;
            }
        }
        return null;
    }

    public Set<GameData> getAllGames() {
        return gameData;
    }

    public boolean addGame(GameData game) {
        return gameData.add(game);
    }
}
