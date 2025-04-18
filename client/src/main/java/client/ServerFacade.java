package client;

import com.google.gson.Gson;
import dto.*;
import exception.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clear() throws ResponseException {
        String path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    public RegisterResponse register(RegisterRequest request) throws ResponseException {
        String path = "/user";
        return this.makeRequest("POST", path, request, RegisterResponse.class, null);
    }

    public LoginResponse login(LoginRequest request) throws ResponseException {
        String path = "/session";
        return this.makeRequest("POST", path, request, LoginResponse.class, null);
    }

    public LogoutResponse logout(LogoutRequest request) throws ResponseException {
        String path = "/session";
        return this.makeRequest("DELETE", path, request, LogoutResponse.class, request.authToken());
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws ResponseException {
        String path = "/game";
        return this.makeRequest("POST", path, request, CreateGameResponse.class, request.authToken());
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws ResponseException {
        String path = "/game";
        return this.makeRequest("GET", path, request, ListGamesResponse.class, request.authToken());
    }

    public JoinGameResponse joinGame(JoinGameRequest request) throws ResponseException {
        String path = "/game";
        return this.makeRequest("PUT", path, request, JoinGameResponse.class, request.authToken());
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String auth) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            if (method.equals("GET")) {
                if (auth != null) {
                    http.addRequestProperty("authorization", auth);
                } else {
                    throw new ResponseException("Error: no auth passed");
                }
            } else {
                http.setDoOutput(true);
                writeBody(request, http, auth);
            }
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream resErr = http.getErrorStream()) {
                if (resErr != null) {
                    throw ResponseException.fromJson(resErr);
                }
            }
            throw new ResponseException("other failure: " + status);
        }
    }

    private static void writeBody(Object request, HttpURLConnection http, String auth) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            if (auth != null) {
                http.addRequestProperty("authorization", auth);
            }
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream resBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(resBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
