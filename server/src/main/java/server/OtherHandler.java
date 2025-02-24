package server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dto.ClearResponse;
import dto.JoinGameResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import model.UserData;
import service.*;
import spark.Request;
import spark.Response;

public class OtherHandler {

    private AuthService authService;
    private final Gson gson;

    public OtherHandler() {
        authService = null;
        gson = new Gson();
    }

    public void setService(AuthService authService) {
        this.authService = authService;
    }

    public String handleClear(Response res) {
        ClearResponse response = authService.clear();

        if (response.message() != null) {
            res.status(500);
        } else {
            res.status(200);
        }

        res.type("application/json");
        return gson.toJson(response);
    }

    public String handleRegister(Request request, Response res) {
        UserData user = gson.fromJson(request.body(), UserData.class);

        if (user.username() == null || user.password() == null || user.email() == null) {
            res.status(400);
            return gson.toJson(new RegisterResponse(null, null, "Error: bad request"));
        }

        RegisterResponse response = authService.register(new RegisterRequest(user));

        if (response.message() != null) {
            if (response.message().contains("already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
        }
        res.type("application/json");

        return gson.toJson(response);
    }
}
