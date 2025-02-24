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

public class SessionHandler {

    private UserService userService;
    private final Gson gson;

    public SessionHandler() {
        userService = null;
        gson = new Gson();
    }

    public void setServices(UserService userService) {
        this.userService = userService;
    }

    public String login(Request req, Response res) {
        UserData user = gson.fromJson(req.body(), UserData.class);
        LoginResponse response = userService.login(new LoginRequest(user));

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

    public String logout(Request req, Response res) {
        String token = gson.fromJson(req.headers("authorization"), String.class);
        LogoutResponse response = userService.logout(new LogoutRequest(token));

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