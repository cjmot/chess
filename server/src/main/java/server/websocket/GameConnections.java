package server.websocket;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class GameConnections {
    public String whitePlayer;
    public String blackPlayer;
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();


    public void add(String token, Connection connection) {
        connections.put(token, connection);
    }

    public void remove(String token) {
        if (Objects.equals(whitePlayer, token)) {
            whitePlayer = null;
        } else if (Objects.equals(blackPlayer, token)) {
            blackPlayer = null;
        }
        connections.remove(token);
    }
}
