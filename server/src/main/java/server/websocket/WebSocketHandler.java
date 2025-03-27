package server.websocket;

import com.google.gson.Gson;
import dataaccess.sql.SqlAuthAccess;
import dataaccess.sql.SqlGameAccess;
import dto.ListGamesRequest;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections;
    private final AuthService authService;
    private final GameService gameService;

    public WebSocketHandler(AuthService authService, GameService gameService) {
        this.connections = new ConnectionManager();
        this.authService = authService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command, session);
        }
    }

    private void connect(UserGameCommand command, Session session) throws IOException {

        String username = verifyAuth(command.getAuthToken());
        if (username == null) {
            String message = "Error: unauthorized";
            ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
        }
        if (!gameService.verifyGameID(command.getGameID())) {
            String message = "Error: could not verify GameID";
            ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
        }

        connections.add(username, session);
        String message = String.format("")
    }

    private String verifyAuth(String authToken) {
        AuthData auth = authService.verifyAuth(authToken);
        if (auth.message() != null) {
            return null;
        }
        return auth.username();
    }
}
