package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.ServerMessageHandler;
import client.websocket.WebSocketFacade;
import dto.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import client.ui.GameUI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static client.EscapeSequences.*;

public class ChessClient {

    public enum State {
        SIGNEDOUT,
        SIGNEDIN,
        GAMESTATE
    }

    private final ServerFacade server;
    private final String serverUrl;
    private final ServerMessageHandler messageHandler;
    private WebSocketFacade ws;
    public State state = State.SIGNEDOUT;
    public AuthData auth;
    private final ConcurrentHashMap<Integer, GameData> games;
    public GameData currentGame = null;
    private boolean resigning = false;


    public ChessClient(String serverUrl, ServerMessageHandler messageHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.messageHandler = messageHandler;
        games = new ConcurrentHashMap<>();
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd;
            if (tokens.length > 0) {
                cmd = tokens[0];
            } else {
                cmd = "help";
            }
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "resign" -> resign(params);
                case "quit" -> "Thank you for playing!";
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "redraw" -> redraw(params);
                case "leave" -> leaveGame(params);
                case "move" -> move(params);
                case "highlight" -> highlight(params);
                default -> help();
            };
        } catch (ResponseException e) {
            return RED + e.getMessage();
        }
    }

    public String help() {
        var result = new StringBuilder();
        List<String> prompts;

        if (state == State.SIGNEDOUT) {
            prompts = List.of(
                    "register <USERNAME> <PASSWORD> <EMAIL>" + PURPLE + " - create a new user",
                    "login <USERNAME> <PASSWORD>" + PURPLE + " - sign in an existing user",
                    "quit" + PURPLE + " - exit game interface",
                    "help" + PURPLE + " - show possible commands"
            );
        } else if (state == State.SIGNEDIN) {
            prompts = List.of(
                    "list" + PURPLE + " - show all games",
                    "create <NAME>" + PURPLE + " - create a new game",
                    "join <ID> <WHITE|BLACK>" + PURPLE + " - join an existing game",
                    "logout" + PURPLE + " - logout user",
                    "quit" + PURPLE + " - exit game interface",
                    "help" + PURPLE + " - show possible commands"
            );
        } else {
            prompts = List.of(
                    "move <STARTPOSITION> <ENDPOSITION>" + PURPLE + " - make a move (ex. move c2 c4)",
                    "highlight <POSITION>" + PURPLE + " - highlight possible moves for a piece",
                    "redraw" + PURPLE + " - redraw the chessboard",
                    "resign" + PURPLE + " - resign and end the game",
                    "leave" + PURPLE + " - leave the game",
                    "help" + PURPLE + " - show possible commands"
            );
        }
        for (String prompt : prompts) {
            result.append(GREEN).append(prompt).append("\n");
        }
        return result.toString();
    }

    public String register(String... params) throws ResponseException {
        checkSignedOut("register");
        if (params.length == 3) {
            RegisterRequest request = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResponse response = server.register(request);
            if (response.authToken() != null) {
                this.auth = new AuthData(response.username(), response.authToken(), null);
                this.state = State.SIGNEDIN;
                return "Successfully registered " + params[0] + "\n";
            }
        }
        throw new ResponseException("Expected: register <username> <password> <email>\n");
    }

    public String login(String... params) throws ResponseException {
        checkSignedOut("login");
        if (params.length == 2) {
            LoginRequest request = new LoginRequest(params[0], params[1]);
            LoginResponse response = server.login(request);
            if (response.authToken() != null) {
                this.auth = new AuthData(response.username(), response.authToken(), null);
                state = State.SIGNEDIN;
                return "Successfully logged in as " + params[0] + "\n";
            }
        }
        throw new ResponseException("Expected: login <username> <password>\n");
    }

    public String logout() throws ResponseException {
        checkSignedIn("logout");
        LogoutRequest request = new LogoutRequest(auth.authToken());
        server.logout(request);
        this.auth = null;
        state = State.SIGNEDOUT;
        return "Successfully logged out\n";
    }

    public String listGames() throws ResponseException {
        checkSignedIn("list");
        ListGamesRequest request = new ListGamesRequest(auth.authToken());
        ListGamesResponse response = server.listGames(request);

        response.games().forEach(game -> games.put(game.gameID(), game));

        var result = new StringBuilder();
        for (GameData game : games.values()) {
            if (game.whiteUsername() == null) {
                game.setWhiteUsername(("____"));
            }
            if (game.blackUsername() == null) {
                game.setBlackUsername("____");
            }
            result.append(String.format(
                            "%d\n\tGameName: %s,\n\tPlayingWhite: %s,\n\tPlayingBlack: %s\n\tGameOver: %s",
                            game.gameID(),
                            game.gameName(),
                            game.whiteUsername(),
                            game.blackUsername(),
                            game.gameOver())
            ).append("\n");
        }
        return result.toString();
    }

    public String createGame(String... params) throws ResponseException {
        checkSignedIn("create");
        if (params.length == 1) {
            CreateGameRequest request = new CreateGameRequest(params[0], auth.authToken());
            CreateGameResponse response = server.createGame(request);
            games.put(
                    response.gameID(),
                    new GameData(response.gameID(), null, null, params[0], new ChessGame())
            );
            return String.format("Created game %s with GameID: %d\n", params[0], response.gameID());
        }
        throw new ResponseException("Expected: create <GameName>");
    }

    public String joinGame(String... params) throws ResponseException {
        checkSignedIn("join");
        if (params.length == 2) {
            String gameID = params[0];
            String color = params[1];
            if (color.equals("white")) {
                color = "WHITE";
            }
            else if (color.equals("black")) {
                color = "BLACK";
            }
            else {
                throw new ResponseException("Color not specified: should be 'white' or 'black'\n");
            }
            JoinGameRequest request = new JoinGameRequest(color, Integer.parseInt(gameID), auth.authToken());
            server.joinGame(request);

            state = State.GAMESTATE;
            ws = new WebSocketFacade(serverUrl, messageHandler, Integer.parseInt(gameID));
            ws.connectToGame(auth.authToken(), Integer.parseInt(gameID));

            return "Joined game " + gameID + "\n";
        }
        throw new ResponseException("Expected: join <gameID> <color>\n");
    }

    public String observeGame(String... params) throws ResponseException {
        checkSignedIn("observe");
        if (params.length == 1) {
            int gameID = Integer.parseInt(params[0]);
            if (games.get(gameID) == null) {
                return "Error: No game found with GameID: " + gameID + "\n";
            }

            state = State.GAMESTATE;
            ws = new WebSocketFacade(serverUrl, messageHandler, gameID);
            ws.connectToGame(auth.authToken(), gameID);

            return "Observing game " + gameID + "...\n";
        }
        throw new ResponseException("Expected: observe <GameID>\n");
    }

    public String leaveGame(String... params) throws ResponseException {
        checkGameState("leave");
        if (params.length == 0) {
            ws.leaveGame(auth.authToken());
            ws = null;
            currentGame = null;
            state = State.SIGNEDIN;

            return "Successfully left game \n";
        }
        throw new ResponseException("Expected: leave\n");
    }

    public String redraw(String... params) throws ResponseException {
        checkGameState("redraw");
        String color = auth.username().equals(currentGame.blackUsername()) ? "BLACK" : "WHITE";
        if (params.length == 0) {
            String result = new GameUI(currentGame, color).printGame(null);
            String turn = currentGame.gameOver() ?
                    "game over\n" : currentGame.game().getTeamTurn().toString().toLowerCase() + " to move" + "\n";
            return "\n" + result + BLUE + turn;
        }
        throw new ResponseException("Expected: redraw");
    }

    public String resign(String... params) throws ResponseException {
        checkGameState("resign");
        if (params.length == 0) {
            if (confirmResign()) {
                ws.resign(auth.authToken());
                currentGame.setGameOver(true);
            }
            return "";
        }
        throw new ResponseException("Expected: resign\n");
    }

    public String move(String... params) throws ResponseException {
        checkGameState("move");
        if (params.length == 2 || params.length == 3) {
            ChessPosition start = getPosition(params[0]);
            ChessPosition end = getPosition(params[1]);
            ChessMove move;
            if (params.length == 2) {
                if (pawnPromotion(start, end, params.length)) {
                    throw new ResponseException("Expected: move <STARTPOSITION> <ENDPOSITION> <PROMOTIONPIECE>\n");
                }
                move = new ChessMove(start, end, null);
            } else {
                if (pawnPromotion(start, end, params.length)) {
                    move = new ChessMove(start, end, getPromoPiece(params[2]));
                } else {
                    throw new ResponseException("Expected: move <STARTPOSITION> <ENDPOSITION>\n");
                }
            }
            ws.makeMove(auth.authToken(), move);
            return "";
        }
        throw new ResponseException("Expected: move <STARTPOSITION> <ENDPOSITION>\n");
    }

    private ChessPiece.PieceType getPromoPiece(String param) throws ResponseException {
        return switch (param) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            case "b" -> ChessPiece.PieceType.BISHOP;
            default -> throw new ResponseException("Error: invalid promotion piece\n");
        };
    }

    private boolean pawnPromotion(ChessPosition start, ChessPosition end, Integer paramsLength) throws ResponseException {
        if (!currentGame.game().getBoard().getPiece(start).getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            if (paramsLength == 2) {
                return false;
            }
            throw new ResponseException("Expected: move <STARTPOSITION> <ENDPOSITION>\n");
        }
        ChessGame.TeamColor color = currentGame.game().getTeamTurn();
        boolean whitePromo = color.equals(ChessGame.TeamColor.WHITE) && end.getRow() == 8;
        boolean blackPromo = color.equals(ChessGame.TeamColor.BLACK) && end.getRow() == 1;
        return whitePromo || blackPromo;
    }

    public String highlight(String... params) throws ResponseException {
        checkGameState("highlight");
        if (params.length == 1) {
            ChessPosition position = getPosition(params[0]);
            if (currentGame.game().getBoard().getPiece(position) == null) {
                throw new ResponseException("Error: no piece at that position");
            }
            String color = auth.username().equals(currentGame.blackUsername()) ? "BLACK" : "WHITE";
            return new GameUI(currentGame, color).printGame(position);
        }
        throw new ResponseException("Expected: highlight <POSITION>\n");
    }

    private boolean confirmResign() {
        printPrompt();
        System.out.print("Are you sure you want to resign? (y/n)");
        printPrompt();

        String ans = new Scanner(System.in).nextLine();
        if (ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
            return true;
        } else if (ans.equalsIgnoreCase("n") || ans.equalsIgnoreCase("no")) {
            return false;
        } else {
            System.out.println(RED + "press 'y' to resign, 'n' to cancel");
            return confirmResign();
        }
    }

    private ChessPosition getPosition(String position) throws ResponseException {
        if (position.length() == 2) {
            ArrayList<String> cols = new ArrayList<>(List.of("a", "b", "c", "d", "e", "f", "g", "h"));
            ArrayList<String> rows = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6", "7", "8"));
            String[] input = position.split("");
            String row = input[1];
            String col = input[0];
            if (cols.contains(col) && rows.contains(row)) {
                return new ChessPosition(rows.indexOf(row) + 1, cols.indexOf(col) + 1);
            }
        }
        throw new ResponseException(String.format("Error: invalid position '%s'\n", position));
    }

    private void checkSignedIn(String action) throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException("Cannot perform '" + action + "' while signed out\n");
        } else if (state == State.GAMESTATE) {
            throw new ResponseException("Cannot perform '" + action + "' while in game\n");
        }
    }

    private void checkSignedOut(String action) throws ResponseException {
        if (state != State.SIGNEDOUT && auth.authToken() != null) {
            throw new ResponseException("Cannot perform '" + action + "' while signed in\n");
        }
    }

    private void checkGameState(String action) throws ResponseException {
        if (state != State.GAMESTATE) {
            throw new ResponseException("Cannot perform '" + action + "' while not in game\n");
        }
    }

    private void printPrompt() {
        String stateString;
        if (state.equals(ChessClient.State.SIGNEDOUT)) {
            stateString = "[LOGGED_OUT]";
        } else if (state.equals(ChessClient.State.SIGNEDIN)) {
            stateString = "[LOGGED_IN]";
        } else {
            stateString = "[IN_GAME]";
        }
        System.out.print("\n" + RESET + stateString + ">>> " + BLUE);
    }
}
