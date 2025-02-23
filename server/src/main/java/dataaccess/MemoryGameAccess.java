package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameAccess implements UserAccess {

    private Collection<GameData> gameData;

    public MemoryGameAccess() {
        gameData = new ArrayList<>();
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

    public Collection<GameData> getGameData() {
        return gameData;
    }

    public boolean addGame(GameData game) {
        return gameData.add(game);
    }
}
