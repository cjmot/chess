package websocket.commands;

public class LeaveCommand extends UserGameCommand {

    public LeaveCommand(UserGameCommand.CommandType type, String authToken, Integer gameID) {
        super(type, authToken, gameID);
    }
}
