package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.ConnectCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, GameConnections> connections = new ConcurrentHashMap<>();

    public void add(ConnectCommand command, Session session) {
        String token = command.getAuthToken();
        Integer gameID = command.getGameID();
        var connection = new Connection(token, session);
        if (connections.get(gameID) == null) {
            GameConnections newGameConnections = new GameConnections();
            connections.put(gameID, newGameConnections);
        }
        connections.get(gameID).add(token, connection);
    }

    public void remove(Integer gameID, String token) {
        connections.get(gameID).remove(token);
    }

    public void broadcast(Integer gameID, String excludeToken, ServerMessage message) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameID).connections.values()) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(excludeToken)) {
                    c.send(message.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        for (var c : removeList) {
            connections.get(gameID).remove(c.authToken);
        }
    }
}
