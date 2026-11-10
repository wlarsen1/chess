package ui;

import client.ServerException;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import java.util.List;

import java.util.Locale;
import java.util.Scanner;

public class ConsoleUI {

    private final ServerFacade facade;
    private AuthData session;
    private List<GameData> listedGames = List.of();

    public ConsoleUI(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to 240 Chess!");
        System.out.println("Type help to see available commands.");

        while (true) {
            String status = session == null ? "LOGGED_OUT" : "LOGGED_IN";
            System.out.print("\n[" + status + "] >>> ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] tokens = input.split("\\s+");
            String command = tokens[0].toLowerCase(Locale.ROOT);

            try {
                switch (command) {
                    case "help" -> {
                        if (tokens.length == 1) {
                            printHelp();
                        } else {
                            System.out.println("Usage: help");
                        }
                    }
                    case "quit" -> {
                        if (tokens.length == 1) {
                            System.out.println("Goodbye!");
                            return;
                        }
                        System.out.println("Usage: quit");
                    }
                    case "register" -> register(tokens);
                    case "login" -> login(tokens);
                    case "logout" -> logout(tokens);
                    case "create" -> createGame(input.substring(tokens[0].length()).strip());
                    case "list" -> listGames(tokens);
                    default -> System.out.println(
                            "Unknown command. Type help to see available commands."
                    );
                }
            } catch (ServerException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void register(String[] tokens) throws ServerException {
        if (session != null) {
            System.out.println("You are already signed in.");
            return;
        }
        if (tokens.length != 4) {
            System.out.println("Usage: register <username> <password> <email>");
            return;
        }

        session = facade.register(tokens[1], tokens[2], tokens[3]);
        System.out.println("Registered and signed in as " + session.username() + ".");
    }

    private void login(String[] tokens) throws ServerException {
        if (session != null) {
            System.out.println("You are already signed in.");
            return;
        }
        if (tokens.length != 3) {
            System.out.println("Usage: login <username> <password>");
            return;
        }

        session = facade.login(tokens[1], tokens[2]);
        System.out.println("Signed in as " + session.username() + ".");
    }

    private void logout(String[] tokens) throws ServerException {
        if (session == null) {
            listedGames = List.of();
            System.out.println("You are already signed out.");
            return;
        }

        if (tokens.length != 1) {
            System.out.println("Usage: logout");
            return;
        }

        facade.logout(session.authToken());
        session = null;
        System.out.println("Signed out.");
    }

    private void createGame(String gameName) throws ServerException {
        if (session == null) {
            System.out.println("Sign in to create a game.");
            return;
        }

        if (gameName.isBlank()) {
            System.out.println("Usage: create <game name>");
            return;
        }

        facade.createGame(session.authToken(), gameName);
        System.out.println("Created game: " + gameName);
    }

    private void listGames(String[] tokens) throws ServerException {
        if (session == null) {
            System.out.println("Sign in to list games.");
            return;
        }

        if (tokens.length != 1) {
            System.out.println("Usage: list");
            return;
        }

        listedGames = facade.listGames(session.authToken());

        if (listedGames.isEmpty()) {
            System.out.println("No games yet. Use create <game name> to start one.");
            return;
        }

        for (int index = 0; index < listedGames.size(); index++) {
            GameData game = listedGames.get(index);
            String white = game.whiteUsername() == null ? "Open" : game.whiteUsername();
            String black = game.blackUsername() == null ? "Open" : game.blackUsername();

            System.out.printf("%d. %s | White: %s | Black: %s%n",
                    index + 1, game.gameName(), white, black);
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");

        if (session == null) {
            System.out.println("  register <username> <password> <email> - create an account");
            System.out.println("  login <username> <password> - sign in");
        } else {
            System.out.println("  create <game name> - create a game");
            System.out.println("  list - show available games");
            System.out.println("  logout - sign out");
        }

        System.out.println("  help - show available commands");
        System.out.println("  quit - exit the program");
    }
}
