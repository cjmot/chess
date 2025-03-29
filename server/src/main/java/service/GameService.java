package service;

import chess.ChessGame;
import dataaccess.sql.SqlAuthAccess;
import dataaccess.sql.SqlDatabaseManager;
import dataaccess.sql.SqlGameAccess;
import dto.ListGamesRequest;
import dto.ListGamesResponse;
import dto.CreateGameRequest;
import dto.CreateGameResponse;
import dto.JoinGameRequest;
import dto.JoinGameResponse;
import model.*;

import java.util.Objects;

public class GameService {

    private final SqlGameAccess gameAccess;
    private final SqlAuthAccess authAccess;

    public GameService(SqlDatabaseManager dbManager) {
        gameAccess = dbManager.gameAccess();
        authAccess = dbManager.authAccess();
    }

    public ListGamesResponse listGames(ListGamesRequest req) {
        if (authAccess.getAuth(req.authToken()).message() != null) {
            return new ListGamesResponse(null, "Error: unauthorized");
        }
        return gameAccess.getAllGames();
    }

    public CreateGameResponse createGame(CreateGameRequest req) {
        if (authAccess.getAuth(req.authToken()).message() != null) {
            return new CreateGameResponse(null, "Error: unauthorized");
        }

        GameData newGame = new GameData(
                null,
                null,
                null,
                req.gameName(),
                new ChessGame()
        );
        String addedMessage = gameAccess.addGame(newGame);
        if (addedMessage.contains("Failed to add game")) {
            if (addedMessage.contains("Duplicate entry")) {
                return new CreateGameResponse(null, "GameName already taken");
            }
            return new CreateGameResponse(null, addedMessage);
        }

        return new CreateGameResponse(Integer.parseInt(addedMessage), null);
    }

    public JoinGameResponse joinGame(JoinGameRequest req) {
        AuthData auth = authAccess.getAuth(req.authToken());
        if (auth.message() != null) {
            return new JoinGameResponse("Error: unauthorized");
        }
        if (!Objects.equals(req.playerColor(), "WHITE") && !"BLACK".equals(req.playerColor())) {
            return new JoinGameResponse("Error: bad player color");
        }
        String updateMessage = gameAccess.updateGame(req.playerColor(), req.gameID(), auth.username());
        if (updateMessage != null) {
            return new JoinGameResponse(updateMessage);
        }

        return new JoinGameResponse(null);
    }

    public boolean leaveGame(String playerColor, Integer gameID) {
        return gameAccess.leaveGame(playerColor, gameID);
    }

    public GameData verifyGameID(Integer gameID) {
        ListGamesResponse response = gameAccess.getAllGames();
        return response.games().stream()
                .filter((game) -> game.gameID().equals(gameID)).findFirst().orElse(null);
    }
}
