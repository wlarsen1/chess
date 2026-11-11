package ui;

import client.ServerException;
import client.ServerFacade;
import model.AuthData;
import model.GameData;
import java.util.List;
import chess.ChessBoard;
import chess.ChessGame.TeamColor;

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
                    case "join" -> joinGame(tokens);
                    case "observe" -> observeGame(tokens);
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

    private void joinGame(String[] tokens) throws ServerException {
        if (session == null) {
            System.out.println("Sign in to join a game.");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Usage: join <game number> <white|black>");
            return;
        }

        TeamColor color = switch (tokens[2].toLowerCase(Locale.ROOT)) {
            case "white" -> TeamColor.WHITE;
            case "black" -> TeamColor.BLACK;
            default -> throw new ServerException("Choose white or black.");
        };

        GameData game = selectListedGame(tokens[1]);
        facade.joinGame(session.authToken(), color.name(), game.gameID());

        System.out.println("Joined " + game.gameName() + " as "
                + color.name().toLowerCase(Locale.ROOT) + ".");
        printStartingBoard(color);
    }

    private void observeGame(String[] tokens) throws ServerException {
        if (session == null) {
            System.out.println("Sign in to observe a game.");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Usage: observe <game number>");
            return;
        }

        GameData game = selectListedGame(tokens[1]);

        System.out.println("Observing " + game.gameName() + ".");
        printStartingBoard(TeamColor.WHITE);
    }

    private GameData selectListedGame(String input) throws ServerException {
        if (listedGames.isEmpty()) {
            throw new ServerException("Use list to see available game numbers.");
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new ServerException("Enter a game number from the list.");
        }

        if (gameNumber < 1 || gameNumber > listedGames.size()) {
            throw new ServerException("Choose a game number from your last list.");
        }

        return listedGames.get(gameNumber - 1);
    }

    private void printStartingBoard(TeamColor perspective) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.print(BoardRenderer.render(board, perspective));
    }

    private void printHelp() {
        System.out.println("Available commands:");

        if (session == null) {
            System.out.println("  register <username> <password> <email> - create an account");
            System.out.println("  login <username> <password> - sign in");
        } else {
            System.out.println("  create <game name> - create a game");
            System.out.println("  list - show available games");
            System.out.println("  join <game number> <white|black> - play a game");
            System.out.println("  observe <game number> - view a game");
            System.out.println("  logout - sign out");
        }

        System.out.println("  help - show available commands");
        System.out.println("  quit - exit the program");
    }
}
