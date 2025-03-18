package client;

import java.util.Scanner;

import static client.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
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
                System.out.print(BLUE + result);
            } catch (Exception e) {
                String msg = e.getMessage();
                System.out.print(RED + msg);
            }
        }
        System.out.println();
    }

    public void printPrompt() {
        String state = ChessClient.State.SIGNEDOUT == client.state ? "[LOGGED_OUT]" : "[LOGGED_IN]";
        System.out.print("\n" + RESET + state + ">>> " + GREEN);
    }
}
