package server;

import com.google.gson.Gson;
import dto.ClearResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import model.UserData;
import service.*;
import spark.Request;
import spark.Response;

public class AuthHandler implements Handler {

    private AuthService authService;
    private final Gson gson;

    public AuthHandler() {
        authService = null;
        gson = new Gson();
    }

    public void setService(AuthService authService) {
        this.authService = authService;
    }

    public String handleClear(Request req, Response res) {
        if (notValidJson(req.body()) || gson.fromJson(req.body(), String.class) != null) {
            res.status(500);
            return gson.toJson(new ClearResponse("Error: bad request"));
        }
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
        if (notValidJson(request.body())) {
            res.status(500);
            return gson.toJson(new RegisterResponse(null, null, "Error: bad request"));
        }
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
