package client;

import java.util.Scanner;

import static client.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {

        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to Chess! Sign in to start.");
        System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit") || !result.equals("q")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.println(BLUE + result);
            } catch (Throwable e) {
                String msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }
}
