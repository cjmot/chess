package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameAccess implements UserAccess {

    private Collection<GameData> gameData;

    public MemoryGameAccess() {
        gameData = new ArrayList<>();
    }

    public void clear() {
        gameData.clear();
    }

    public Collection<GameData> getGameData() {
        return gameData;
    }

    public boolean addGame(GameData game) {
        return gameData.add(game);
    }
}
