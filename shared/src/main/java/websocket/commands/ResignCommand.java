package websocket.commands;

public class ResignCommand extends UserGameCommand {

    public ResignCommand(CommandType type, String authToken, Integer gameID) {
        super(type, authToken, gameID);
    }
}
