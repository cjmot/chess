package websocket.messages;

import com.google.gson.Gson;
import model.GameData;

public class LoadGameMessage extends ServerMessage {

    private final GameData game;
    private final transient Gson gson;

    public LoadGameMessage(ServerMessageType type, GameData game) {
        super(type);
        this.game = game;
        this.gson = new Gson();
    }

    private GameData getGame() {
        return this.game;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
