package server.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.commands.*;
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
        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("commandType").getAsString();
        switch (type) {
            case "CONNECT":
                ConnectCommand command = gson.fromJson(message, ConnectCommand.class);
                connect(command, session);
                break;
        }
    }

    private void connect(ConnectCommand command, Session session) throws IOException {

        if (!verifyInfo(command, session)) {
            return;
        }

        connections.add(command.getAuthToken(), session);
        String message = String.format("You joined game %d as", command.getGameID());

        ServerMessage rootMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        session.getRemote().sendString(rootMessage.toString());

        message = String.format(
                "joined game %d as",
                command.getGameID()
        );
        var usersMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(command.getAuthToken(), usersMessage);
    }

    private boolean verifyInfo(ConnectCommand command, Session session) throws IOException {
        AuthData auth = authService.verifyAuth(command.getAuthToken());
        if (auth.message() != null) {
            String message = "Error: could not verify authToken";
            ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
            return false;
        }
        if (!gameService.verifyGameID(command.getGameID())) {
            String message = "Error: could not verify GameID";
            ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
            return false;
        }
        return true;
    }
}
