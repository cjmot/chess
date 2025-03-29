package websocket.messages;

import com.google.gson.Gson;

public class ErrorMessage extends ServerMessage {

    private final String errorMessage;
    private final transient Gson gson;

    public ErrorMessage(ServerMessageType type, String errorMessage) {
        super(type);
        this.errorMessage = errorMessage;
        this.gson = new Gson();
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
