package server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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

    public void setService(UserService userService) {
        this.userService = userService;
    }

    public String login(Request req, Response res) {
        if (!isValidJson(req.body())) {
            res.status(401);
            return gson.toJson(new LoginResponse(null, null, "Error: unauthorized"));
        }
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
        if (!isValidJson(req.headers("authorization"))) {
            res.status(401);
            return gson.toJson(new LogoutResponse("Error: unauthorized"));
        }
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

    private boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}