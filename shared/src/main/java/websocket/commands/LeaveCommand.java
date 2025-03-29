package websocket.commands;

public class LeaveCommand extends UserGameCommand {

    private final String playerColor;

    public LeaveCommand(UserGameCommand.CommandType type, String authToken, Integer gameID, String playerColor) {
        super(type, authToken, gameID);
        this.playerColor = playerColor;
    }

    public String getPlayerColor() {
        return this.playerColor;
    }
}
