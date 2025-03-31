package server.websocket;

import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections;
    private final AuthService authService;
    private final GameService gameService;

    private final String[] rows = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private final String[] cols = {"h", "g", "f", "e", "d", "c", "b", "a"};

    public WebSocketHandler(AuthService authService, GameService gameService) {
        this.connections = new ConnectionManager();
        this.authService = authService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("commandType").getAsString();
        switch (type) {
            case "CONNECT":
                ConnectCommand connectCmd = gson.fromJson(message, ConnectCommand.class);
                connect(connectCmd, session);
                break;
            case "LEAVE":
                LeaveCommand leaveCmd = gson.fromJson(message, LeaveCommand.class);
                leave(leaveCmd, session);
                break;
            case "RESIGN":
                ResignCommand resignCmd = gson.fromJson(message, ResignCommand.class);
                resign(resignCmd, session);
                break;
            case "MAKE_MOVE":
                MakeMoveCommand moveCmd = gson.fromJson(message, MakeMoveCommand.class);
                makeMove(moveCmd, session);
                break;
        }
    }

    private void connect(ConnectCommand command, Session session) throws IOException {
        AuthData auth = getAuth(command.getAuthToken(), session);
        if (auth == null) {
            return;
        }

        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }

        connections.add(command, session);

        LoadGameMessage rootMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        session.getRemote().sendString(rootMessage.toString());

        String message = String.format(
                "%s joined game as %s", auth.username(), command.getConnType().toString().toLowerCase()
        );
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
    }

    private void leave(LeaveCommand command, Session session) throws IOException {
        AuthData auth = getAuth(command.getAuthToken(), session);
        if (auth == null) {
            return;
        }

        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }
        if (!gameService.leaveGame(command.getPlayerColor(), command.getGameID())) {
            String message = "Error: could not leave game";
            ServerMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
            return;
        }

        connections.remove(command.getGameID(), command.getAuthToken());

        String message = String.format("%s has left the game", auth.username());
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
    }

    private void resign(ResignCommand command, Session session) throws IOException {
        AuthData auth = getAuth(command.getAuthToken(), session);
        if (auth == null) {
            return;
        }

        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }
        if (!gameService.markGameOver(command.getGameID())) {
            String message = "Error: failed to resign";
            ErrorMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
            return;
        }
        String message = String.format("%s has resigned", auth.username());
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(command.getGameID(), null, notification);
    }

    private void makeMove(MakeMoveCommand command, Session session) throws IOException {
        AuthData auth = authService.verifyAuth(command.getAuthToken());
        if (auth == null) {
            return;
        }

        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }

        ChessMove moveToMake = command.getMove();
        try {
            game.game().makeMove(moveToMake);
            if (!gameService.updateGame(game)) {
                String message = "Error: failed to make move";
                ErrorMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
                session.getRemote().sendString(response.toString());
            } else {
                var notification = getNotification(moveToMake, auth);
                connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
                LoadGameMessage loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                connections.broadcast(command.getGameID(), null, loadGame);
            }
        } catch (InvalidMoveException e) {
            String message = "Error: invalid move";
            ErrorMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
        }
    }

    private Notification getNotification(ChessMove moveToMake, AuthData auth) {
        ChessPosition startPos = moveToMake.getStartPosition();
        ChessPosition endPos = moveToMake.getEndPosition();
        String start = String.format("%s%s", cols[startPos.getColumn() - 1], rows[startPos.getRow() - 1]);
        String end = String.format("%s%s", cols[endPos.getColumn() - 1], rows[endPos.getRow() - 1]);
        String message = String.format("%s made move: %s to %s", auth.username(), start, end);
        return new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
    }

    private AuthData getAuth(String authToken, Session session) throws IOException {
        AuthData auth = authService.verifyAuth(authToken);
        if (auth.message() != null) {
            String message = "Error: could not verify authToken";
            ServerMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
            return null;
        }
        return auth;
    }

    private GameData verifyGame(Integer gameID, Session session) throws IOException {
        GameData game = gameService.verifyGameID(gameID);
        if (game == null) {
            String message = String.format("Error: no game with GameID %d", gameID);
            ServerMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(response.toString());
            return null;
        }
        return game;
    }
}
