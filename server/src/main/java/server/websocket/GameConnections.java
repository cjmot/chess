package server.websocket;

import java.util.concurrent.ConcurrentHashMap;

public class GameConnections {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String token, Connection connection) {
        connections.put(token, connection);
    }

    public void remove(String token) {
        connections.remove(token);
    }
}
