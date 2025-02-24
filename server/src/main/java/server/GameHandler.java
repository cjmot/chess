package server;

import com.google.gson.Gson;
import dto.CreateGameRequest;
import dto.CreateGameResponse;
import dto.ListGamesRequest;
import dto.ListGamesResponse;
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
}
