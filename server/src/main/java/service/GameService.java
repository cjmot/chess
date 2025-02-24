package service;

import chess.ChessGame;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import model.*;

import java.util.Set;

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
        if (authAccess.getAuth(req.authToken()) == null) {
            return new ListGamesResponse(null, "Error: unauthorized");
        }

        Set<GameData> games = gameAccess.getAllGames();
        if (games == null) {
            return new ListGamesResponse(null, "Failed to get games");
        }
        return new ListGamesResponse(games, null);
    }

    public CreateGameResponse createGame(CreateGameRequest req) {
        if (authAccess.getAuth(req.authToken()) == null) {
            return new CreateGameResponse(null, "Error: unauthorized");
        }

        GameData newGame = new GameData(
                gameAccess.getAllGames().size() + 1,
                null,
                null,
                req.gameName(),
                new ChessGame()
        );
        String addedMessage = gameAccess.addGame(newGame);
        if (addedMessage != null) {
            return new CreateGameResponse(null, addedMessage);
        }

        return new CreateGameResponse(newGame.gameID(), null);
    }

    public JoinGameResponse joinGame(JoinGameRequest req) {
        AuthData auth = authAccess.getAuth(req.authToken());
        if (auth == null) {
            return new JoinGameResponse("Error: unauthorized");
        }

        String updateMessage = gameAccess.updateGame(req.playerColor(), req.gameID(), auth.username());
        if (updateMessage != null) {
            return new JoinGameResponse(updateMessage);
        }

        return new JoinGameResponse(null);


    }
}
