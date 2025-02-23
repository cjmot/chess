package service;

import dataaccess.MemoryGameAccess;
import dto.ClearResponse;

public class GameService {

    private MemoryGameAccess gameAccess;

    public GameService() {
        gameAccess = null;
    }

    public void setGameAccess(MemoryGameAccess gameAccess) {
        this.gameAccess = gameAccess;
    }

    public ClearResponse clearGameData() {
        return new ClearResponse(gameAccess.clear());
    }
}
