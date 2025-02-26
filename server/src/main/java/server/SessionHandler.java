package server;

import com.google.gson.Gson;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.LogoutRequest;
import dto.LogoutResponse;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;

public class SessionHandler implements Handler {

    private final UserService userService;
    private final Gson gson;

    public SessionHandler(UserService userService) {
        this.userService = userService;
        gson = new Gson();
    }

    public String login(Request req, Response res) {
        if (notValidJson(req.body())) {
            res.status(401);
            return gson.toJson(new LoginResponse(null, null, "Error: unauthorized"));
        }
        UserData user = gson.fromJson(req.body(), UserData.class);
        LoginResponse response = userService.login(new LoginRequest(user));

        if (response.message() != null) {
            if (response.message().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
        } else {
            res.status(200);
        }

        res.type("application/json");
        return gson.toJson(response);
    }

    public String logout(Request req, Response res) {
        if (notValidJson(req.headers("authorization"))) {
            res.status(401);
            return gson.toJson(new LogoutResponse("Error: unauthorized"));
        }
        String token = gson.fromJson(req.headers("authorization"), String.class);
        LogoutResponse response = userService.logout(new LogoutRequest(token));

        if (response.message() != null) {
            if (response.message().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
        } else {
            res.status(200);
        }

        res.type("application/json");
        return gson.toJson(response);
    }
}