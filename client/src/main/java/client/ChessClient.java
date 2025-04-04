package client;

import chess.ChessGame;
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
                case "resign" -> resign(params);
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
                    "move <STARTPOSITION> <ENDPOSITION>" + PURPLE + " - make a move",
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
                            "%d\n\tGameName: %s,\n\tPlayingWhite: %s,\n\tPlayingBlack: %s",
                            game.gameID(),
                            game.gameName(),
                            game.whiteUsername(),
                            game.blackUsername())
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
            String result = new GameUI(currentGame, color).printGame();
            String turn = currentGame.gameOver() ?
                    "game over\n" : currentGame.game().getTeamTurn().toString().toLowerCase() + " to move" + "\n";
            return "\n" + result + BLUE + turn;
        }
        throw new ResponseException("Expected: redraw");
    }

    public String resign(String... params) throws ResponseException {
        checkGameState("resign");
        if (params.length == 0) {
            ws.resign(auth.authToken());
            currentGame.setGameOver(true);
            return "";
        }
        throw new ResponseException("Expected: resign");
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
}
