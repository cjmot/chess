package client.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import exception.ResponseException;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private final Gson gson = createSerializer();

    Session session;
    ServerMessageHandler serverMessageHandler;

    public WebSocketFacade(String url, ServerMessageHandler handler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            serverMessageHandler = handler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    serverMessageHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }


    public void observeGame(String authToken, Integer gameID) throws ResponseException {
        try {
            var command = new ConnectCommand(CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ServerMessage.class,
                (JsonDeserializer<ServerMessage>) (el, type, ctx) -> {
            ServerMessage message = null;
            if (el.isJsonObject()) {
                String messageType = el.getAsJsonObject().get("serverMessageType").getAsString();
                switch (ServerMessage.ServerMessageType.valueOf(messageType)) {
                    case NOTIFICATION -> message = ctx.deserialize(el, Notification.class);
                    case ERROR -> message = ctx.deserialize(el, ErrorMessage.class);
                    case LOAD_GAME -> message = ctx.deserialize(el, LoadGameMessage.class);
                }
            }
            return message;
            });
        return gsonBuilder.create();
    }
}
