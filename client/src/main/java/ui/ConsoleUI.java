package ui;

import java.util.Locale;
import java.util.Scanner;

public class ConsoleUI {

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to 240 Chess!");
        System.out.println("Type help to see available commands.");

        while (true) {
            System.out.print("\n[LOGGED_OUT] >>> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] tokens = input.split("\\s+");
            String command = tokens[0].toLowerCase(Locale.ROOT);

            switch (command) {
                case "help" -> {
                    if (tokens.length != 1) {
                        System.out.println("Usage: help");
                    } else {
                        System.out.println(help());
                    }
                }
                case "quit" -> {
                    if (tokens.length != 1) {
                        System.out.println("Usage: quit");
                    } else {
                        System.out.println("Goodbye!");
                        return;
                    }
                }
                default ->
                        System.out.println(
                                "Unknown command. Type help to see available commands."
                        );
            }
        }
    }

    private String help() {
        return """
                Available commands:
                  help - show available commands
                  quit - exit the program
                """;
    }
}
