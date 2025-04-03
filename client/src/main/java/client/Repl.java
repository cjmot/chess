package client;

import client.ui.GameUI;
import client.websocket.ServerMessageHandler;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static client.EscapeSequences.*;

public class Repl implements ServerMessageHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to chess! Sign in or register to start.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(GREEN + result + RESET);
            } catch (Exception e) {
                String msg = e.getMessage();
                System.out.print(RED + msg + RESET);
            }
        }
        System.out.println();
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> displayNotification(((Notification) message).getMessage()   );
            case ERROR -> displayError(((ErrorMessage) message).getErrorMessage());
            case LOAD_GAME -> loadGame(((LoadGameMessage) message).getGame());
        }
        printPrompt();
    }

    public void printPrompt() {
        String state;
        if (client.state.equals(ChessClient.State.SIGNEDOUT)) {
            state = "[LOGGED_OUT]";
        } else if (client.state.equals(ChessClient.State.SIGNEDIN)) {
            state = "[LOGGED_IN]";
        } else {
            state = "[IN_GAME]";
        }
        System.out.print("\n" + RESET + state + ">>> " + BLUE);
    }

    private void displayNotification(String message) {
        System.out.print(BLUE + message);
    }

    private void displayError(String message) {
        System.out.print(RED + message);
    }

    private void loadGame(GameData game) {
        String result = new GameUI(game, game.game().getTeamTurn().toString()).printGame();
        System.out.print("\n" + result);
    }
}
