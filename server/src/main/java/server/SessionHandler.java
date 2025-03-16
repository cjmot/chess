package server;

import com.google.gson.Gson;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.LogoutRequest;
import dto.LogoutResponse;
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

    public String login(Request jsonReq, Response res) {
        if (notValidJson(jsonReq.body())) {
            res.status(401);
            return gson.toJson(new LoginResponse(null, null, "Error: unauthorized"));
        }
        LoginRequest request = gson.fromJson(jsonReq.body(), LoginRequest.class);
        LoginResponse response = userService.login(request);

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
        String token = req.headers("Authorization");
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