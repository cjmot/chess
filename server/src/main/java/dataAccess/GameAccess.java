package dataAccess;

import model.GameData;

import java.util.Set;

public interface GameAccess {
    String clear();

    Set<GameData> getAllGames();

    String addGame(GameData game);

    String updateGame(String playerColor, Integer gameID, String username);
}

