package websocket.commands;

public class ConnectCommand extends UserGameCommand {

    public ConnectCommand(CommandType type, String authToken, Integer gameID) {
        super(type, authToken, gameID);
    }
}
