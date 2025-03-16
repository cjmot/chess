package client;

import dto.RegisterRequest;
import dto.RegisterResponse;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResponse register(RegisterRequest request) {

    }
}
