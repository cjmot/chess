package service;

import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import dto.ListGamesRequest;
import dto.ListGamesResponse;
import model.GameData;

import java.util.Collection;

public class GameService {

    private MemoryGameAccess gameAccess;
    private MemoryAuthAccess authAccess;

    public GameService() {
        gameAccess = null;
        authAccess = null;
    }

    public void setGameAccess(MemoryGameAccess gameAccess, MemoryAuthAccess authAccess) {
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    public ListGamesResponse listGames(ListGamesRequest req) {
        if (!authAccess.getAuth(req.authToken())) {
            return new ListGamesResponse(null, "Error: unauthorized");
        }

        Collection<GameData> games = gameAccess.getAllGames();
        if (games == null) {
            return new ListGamesResponse(null, "Failed to get games");
        }
        return new ListGamesResponse(games, null);
    }
}
