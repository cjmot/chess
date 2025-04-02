package server.websocket;

import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import exception.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;

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
        try {
            Gson gson = createSerializer();
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            String username = getUsername(command.getAuthToken());

            switch (command.getCommandType()) {
                case CONNECT -> connect((ConnectCommand) command, session, username);
                case MAKE_MOVE -> makeMove((MakeMoveCommand) command, session, username);
                case LEAVE -> leave((LeaveCommand) command, session, username);
                case RESIGN -> resign((ResignCommand) command, session, username);
            }
        } catch (UnauthorizedException ue) {
            sendMessage(session.getRemote(), new ErrorMessage("Error: unauthorized"));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(session.getRemote(), new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void sendMessage(RemoteEndpoint remote, ErrorMessage errorMessage) throws IOException {
        remote.sendString(errorMessage.toString());
    }

    private void connect(ConnectCommand command, Session session, String username) throws IOException {
        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }

        connections.add(command, session);

        LoadGameMessage rootMessage = new LoadGameMessage(game);
        session.getRemote().sendString(rootMessage.toString());
        String connType;
        if (username.equals(game.whiteUsername())) {
            connType = "white";
        } else if (username.equals(game.blackUsername())) {
            connType = "black";
        } else {
            connType = "observer";
        }
        String message = String.format(
                "%s joined game as %s", username, connType
        );
        var notification = new Notification(message);
        connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
    }

    private void leave(LeaveCommand command, Session session, String username) throws IOException {
        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }

        boolean player = username.equals(game.whiteUsername()) || username.equals(game.blackUsername());
        if (player) {
            String playerColor = username.equals(game.whiteUsername()) ? "WHITE" : "BLACK";
            if (!gameService.leaveGame(playerColor, command.getGameID())) {
                String message = "Error: could not leave game";
                sendMessage(session.getRemote(), new ErrorMessage(message));
                return;
            }
        }

        connections.remove(command.getGameID(), command.getAuthToken());

        String message = String.format("%s has left the game", username);
        var notification = new Notification(message);
        connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
    }

    private void resign(ResignCommand command, Session session, String username) throws IOException {
        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }
        if (gameOver(game, session, "resign") || isObserver(game, username, session)) {
            return;
        }
        if (!gameService.markGameOver(command.getGameID())) {
            String message = "Error: failed to resign";
            sendMessage(session.getRemote(), new ErrorMessage(message));
            return;
        }
        String message = String.format("%s has resigned", username);
        var notification = new Notification(message);
        connections.broadcast(command.getGameID(), null, notification);
    }

    private void makeMove(MakeMoveCommand command, Session session, String username) throws IOException {

        GameData game = verifyGame(command.getGameID(), session);
        if (game == null) {
            return;
        }
        if (wrongTurn(game, username, session) || gameOver(game, session, "make a move")) {
            return;
        }

        ChessMove moveToMake = command.getMove();
        try {
            game.game().makeMove(moveToMake);
            if (!gameService.updateGame(game)) {
                String message = "Error: failed to make move";
                sendMessage(session.getRemote(), new ErrorMessage(message));
            } else {
                var notification = getNotification(moveToMake, username);
                connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
                LoadGameMessage loadGame = new LoadGameMessage(game);
                connections.broadcast(command.getGameID(), null, loadGame);
            }
        } catch (InvalidMoveException e) {
            String message = "Error: invalid move";
            sendMessage(session.getRemote(), new ErrorMessage(message));
        }
    }

    private boolean gameOver(GameData game, Session session, String command) throws IOException {
        if (game.gameOver()) {
            String message = String.format("Error: cannot %s when game is over", command);
            sendMessage(session.getRemote(), new ErrorMessage(message));
            return true;
        }
        return false;
    }

    private Notification getNotification(ChessMove moveToMake, String username) {
        ChessPosition startPos = moveToMake.getStartPosition();
        ChessPosition endPos = moveToMake.getEndPosition();
        String start = String.format("%s%s", cols[startPos.getColumn() - 1], rows[startPos.getRow() - 1]);
        String end = String.format("%s%s", cols[endPos.getColumn() - 1], rows[endPos.getRow() - 1]);
        String message = String.format("%s made move: %s to %s", username, start, end);
        return new Notification(message);
    }

    private String getUsername(String authToken) throws UnauthorizedException {
        AuthData auth = authService.verifyAuth(authToken);
        if (auth.message() != null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return auth.username();
    }

    private GameData verifyGame(Integer gameID, Session session) throws IOException {
        GameData game = gameService.verifyGameID(gameID);
        if (game == null) {
            String message = String.format("Error: no game with GameID %d", gameID);
            sendMessage(session.getRemote(), new ErrorMessage(message));
            return null;
        }
        return game;
    }

    private boolean wrongTurn(GameData game, String username, Session session) throws IOException {
        String turn = game.game().getTeamTurn().toString().toLowerCase();
        if (username.equals(game.whiteUsername()) && turn.equals("white")) {
            return false;
        } else if (username.equals(game.blackUsername()) && turn.equals("black")) {
            return false;
        }

        String message;
        if (!username.equals(game.blackUsername()) && !username.equals(game.whiteUsername())) {
            message = "Error: cannot make move as observer";
        } else if (username.equals(game.whiteUsername()) && turn.equals("black")) {
            message = "Error: cannot make move on black's turn";
        } else {
            message = "Error: cannot make move on white's turn";
        }
        sendMessage(session.getRemote(), new ErrorMessage(message));
        return true;
    }

    private boolean isObserver(GameData game, String username, Session session) throws IOException {
        boolean observer = !username.equals(game.whiteUsername()) && !username.equals(game.blackUsername());
        if (observer) {
            String message = "Error: cannot resign as observer";
            sendMessage(session.getRemote(), new ErrorMessage(message));
            return true;
        }
        return false;
    }

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
                (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                    UserGameCommand command = null;
                    if (el.isJsonObject()) {
                        String commandType = el.getAsJsonObject().get("commandType").getAsString();
                        switch (UserGameCommand.CommandType.valueOf(commandType)) {
                            case CONNECT -> command = ctx.deserialize(el, ConnectCommand.class);
                            case RESIGN -> command = ctx.deserialize(el, ResignCommand.class);
                            case MAKE_MOVE -> command = ctx.deserialize(el, MakeMoveCommand.class);
                            case LEAVE -> command = ctx.deserialize(el, LeaveCommand.class);
                        }
                    }
                    return command;
                });

        return gsonBuilder.create();
    }
}
