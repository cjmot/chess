package client;

import chess.ChessGame;
import dto.*;
import exception.ResponseException;
import model.GameData;
import ui.GameUI;

import java.util.*;

import static client.EscapeSequences.*;

public class ChessClient {

    public enum State {
        SIGNEDOUT,
        SIGNEDIN
    }

    private final ServerFacade server;
    public State state;
    private String auth;
    private Collection<GameData> games;


    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        state = State.SIGNEDOUT;
        games = new ArrayList<>();
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
                case "quit" -> "quit";
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
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
        } else {
            prompts = List.of(
                    "list" + PURPLE + " - show all games",
                    "create <NAME>" + PURPLE + " - create a new game",
                    "join <ID> <WHITE|BLACK>" + PURPLE + " - join an existing game",
                    "logout" + PURPLE + " - logout user",
                    "quit" + PURPLE + " - exit game interface",
                    "help" + PURPLE + " - show possible commands"
            );
        }
        for (String prompt : prompts) {
            result.append(BLUE).append(prompt).append("\n");
        }
        return result.toString();
    }

    public String register(String... params) throws ResponseException {
        checkSignedOut("register");
        if (params.length == 3) {
            RegisterRequest request = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResponse response = server.register(request);
            if (response.authToken() != null) {
                auth = response.authToken();
                state = State.SIGNEDIN;
                return "Successfully registered " + params[0];
            }
        }
        throw new ResponseException("Expected: register <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        checkSignedOut("login");
        if (params.length == 2) {
            LoginRequest request = new LoginRequest(params[0], params[1]);
            LoginResponse response = server.login(request);
            if (response.authToken() != null) {
                auth = response.authToken();
                state = State.SIGNEDIN;
                return "Successfully logged in as " + params[0];
            }
        }
        throw new ResponseException("Expected: login <username> <password>");
    }

    public String logout() throws ResponseException {
        checkSignedIn("logout");
        LogoutRequest request = new LogoutRequest(auth);
        server.logout(request);
        auth = null;
        state = State.SIGNEDOUT;
        return "Successfully logged out";
    }

    public String listGames() throws ResponseException {
        checkSignedIn("list");
        ListGamesRequest request = new ListGamesRequest(auth);
        ListGamesResponse response = server.listGames(request);

        this.games = response.games();

        var result = new StringBuilder();
        for (GameData game : response.games()) {
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
            CreateGameRequest request = new CreateGameRequest(params[0], auth);
            CreateGameResponse response = server.createGame(request);
            games.add(new GameData(response.gameID(), null, null, params[0], new ChessGame()));
            return String.format("Created game %s with GameID: %d", params[0], response.gameID());
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
                throw new ResponseException("Color not specified: should be 'white' or 'black'");
            }
            JoinGameRequest request = new JoinGameRequest(color, Integer.parseInt(gameID), auth);
            server.joinGame(request);

            GameData gameToJoin = games.stream()
                    .filter(game -> game.gameID().equals(Integer.parseInt(gameID)))
                    .findFirst()
                    .orElseThrow();
            return "Joined game as " + gameID + "\n" + new GameUI(gameToJoin, color).printGame() + "\n" + new GameUI(gameToJoin, "BLACK").printGame();
        }
        throw new ResponseException("Expected: join <gameID> <color>");
    }

    private void checkSignedIn(String action) throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException("Cannot perform '" + action + "' while signed out");
        }
    }

    private void checkSignedOut(String action) throws ResponseException {
        if (state == State.SIGNEDIN && auth != null) {
            throw new ResponseException("Cannot perform '" + action + "' while signed in");
        }
    }
}
