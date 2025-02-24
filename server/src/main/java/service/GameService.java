package service;

import chess.ChessGame;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import dto.CreateGameRequest;
import dto.CreateGameResponse;
import dto.ListGamesRequest;
import dto.ListGamesResponse;
import model.AuthData;
import model.GameData;

import java.util.Set;
import java.util.UUID;

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

        Set<GameData> games = gameAccess.getAllGames();
        if (games == null) {
            return new ListGamesResponse(null, "Failed to get games");
        }
        return new ListGamesResponse(games, null);
    }

    public CreateGameResponse createGame(CreateGameRequest req) {
        if (!authAccess.getAuth(req.authToken())) {
            return new CreateGameResponse(null, "Error: unauthorized");
        }

        GameData newGame = new GameData(
                gameAccess.getAllGames().size() + 1,
                "",
                "",
                req.gameName(),
                new ChessGame()
        );
        String addedMessage = gameAccess.addGame(newGame);
        if (addedMessage != null) {
            return new CreateGameResponse(null, addedMessage);
        }

        return new CreateGameResponse(newGame.gameID(), null);
    }
}
