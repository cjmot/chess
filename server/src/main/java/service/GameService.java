package service;

import dataaccess.MemoryGameAccess;

public class GameService {

    private MemoryGameAccess gameAccess;

    public GameService() {
        gameAccess = null;
    }

    public void setGameAccess(MemoryGameAccess gameAccess) {
        this.gameAccess = gameAccess;
    }
}
