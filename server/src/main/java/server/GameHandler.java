package server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dto.*;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

public class GameHandler {

    private GameService gameService;
    private final Gson gson;

    public GameHandler() {
        gameService = null;
        gson = new Gson();
    }

    public void setService(GameService gameService) {
        this.gameService = gameService;
    }

    public String listGames(Request req, Response res) {
        if (!(isValidJson(req.headers("authorization")))) {
            res.status(401);
            return gson.toJson(new ListGamesResponse(null, "Error: unauthorized"));
        }
        String token = gson.fromJson(req.headers("authorization"), String.class);
        ListGamesResponse response = gameService.listGames(new ListGamesRequest(token));
        if (response.message() != null) {
            if (response.message().contains("unauthorized")) {
                res.status(401);
            } else res.status(500);
        } else {
            res.status(200);
        }
        res.type("application/json");
        return gson.toJson(response);
    }

    public String createGame(Request req, Response res) {
        if (!(isValidJson(req.headers("authorization")))) {
            res.status(401);
            return gson.toJson(new CreateGameResponse(null, "Error: unauthorized"));
        }
        String token = gson.fromJson(req.headers("authorization"), String.class);
        String gameName = gson.fromJson(req.body(), GameData.class).gameName();
        CreateGameResponse response = gameService.createGame(new CreateGameRequest(gameName, token));
        if (response.message() != null) {
            if (response.message().contains("unauthorized")) {
                res.status(401);
            } else res.status(500);
        } else {
            res.status(200);
        }
        res.type("application/json");
        return gson.toJson(response);
    }

    public String joinGame(Request req, Response res) {
        if (!isValidJson(req.headers("authorization"))) {
            res.status(401);
            return gson.toJson(new JoinGameResponse("Error: unauthorized"));
        }
        String token = gson.fromJson(req.headers("authorization"), String.class);
        String playerColor = gson.fromJson(req.body(), JoinGameRequest.class).playerColor();
        Integer gameID = gson.fromJson(req.body(), JoinGameRequest.class).gameID();
        if (
                token == null
                || playerColor == null
                || gameID == null
                || !(playerColor.equals("WHITE") || playerColor.equals("BLACK"))) {
            res.status(400);
            res.type("application/json");
            return gson.toJson(new JoinGameResponse("Error: bad request"));
        }

        JoinGameResponse response = gameService.joinGame(new JoinGameRequest(playerColor, gameID, token));
        if (response.message() != null) {
            if (response.message().contains("unauthorized")) {
                res.status(401);
            } else if (response.message().contains("already taken")) {
                res.status(403);
            } else res.status(500);
        } else {
            res.status(200);
        }

        res.type("application/json");
        return gson.toJson(response);
    }

    private boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}
