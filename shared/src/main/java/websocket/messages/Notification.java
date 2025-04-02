package websocket.messages;

import com.google.gson.Gson;

public class Notification extends ServerMessage {

    private final String message;
    private final transient Gson gson;

    public Notification(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
        this.gson = new Gson();
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
