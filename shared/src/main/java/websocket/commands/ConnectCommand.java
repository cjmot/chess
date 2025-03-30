package websocket.commands;

public class ConnectCommand extends UserGameCommand {

    private final ConnectionType connType;

    public ConnectCommand(CommandType type, String authToken, Integer gameID, ConnectionType connType) {
        super(type, authToken, gameID);
        this.connType = connType;
    }

    public enum ConnectionType {
        WHITE,
        BLACK,
        OBSERVER
    }

    public ConnectionType getConnType() {
        return this.connType;
    }
}
