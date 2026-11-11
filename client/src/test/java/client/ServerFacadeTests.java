package client;

import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clearDatabase() throws ServerException {
        facade.clear();
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void clearEmptyDatabase() {
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    public void rejectInvalidPort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ServerFacade(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ServerFacade(65536)
        );
    }
    @Test
    public void registerSuccess() throws ServerException {
        var auth = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        assertNotNull(auth);
        assertEquals("player1", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    public void registerDuplicateUsername() throws ServerException {
        facade.register("player1", "password", "player1@email.com");

        var error = assertThrows(
                ServerException.class,
                () -> facade.register(
                        "player1",
                        "differentPassword",
                        "different@email.com"
                )
        );

        assertEquals("That username is already taken.", error.getMessage());
    }

    @Test
    public void registerMissingPassword() {
        assertThrows(
                ServerException.class,
                () -> facade.register(
                        "player1",
                        null,
                        "player1@email.com"
                )
        );
    }

    @Test
    public void clearRemovesRegisteredUser() throws ServerException {
        facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        facade.clear();

        var auth = facade.register(
                "player1",
                "newPassword",
                "player1@email.com"
        );

        assertEquals("player1", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    public void loginSuccess() throws ServerException {
        var registration = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        var auth = facade.login("player1", "password");

        assertNotNull(auth);
        assertEquals("player1", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
        assertNotEquals(registration.authToken(), auth.authToken());
    }

    @Test
    public void loginWrongPassword() throws ServerException {
        facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        assertThrows(
                ServerException.class,
                () -> facade.login("player1", "wrongPassword")
        );
    }

    @Test
    public void loginUnknownUser() {
        assertThrows(
                ServerException.class,
                () -> facade.login("unknownPlayer", "password")
        );
    }

    @Test
    public void logoutSuccess() throws ServerException {
        facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        var auth = facade.login("player1", "password");

        assertDoesNotThrow(() -> facade.logout(auth.authToken()));

        assertThrows(
                ServerException.class,
                () -> facade.logout(auth.authToken())
        );
    }

    @Test
    public void logoutInvalidToken() {
        assertThrows(
                ServerException.class,
                () -> facade.logout("invalid-token")
        );
    }

    @Test
    public void logoutMissingToken() {
        assertThrows(
                ServerException.class,
                () -> facade.logout(null)
        );
    }

    @Test
    public void createGameSuccess() throws ServerException {
        var auth = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        int gameID = facade.createGame(
                auth.authToken(),
                "My First Game"
        );

        var games = facade.listGames(auth.authToken());

        assertEquals(1, games.size());
        assertEquals(gameID, games.get(0).gameID());
        assertEquals("My First Game", games.get(0).gameName());
        assertNull(games.get(0).whiteUsername());
        assertNull(games.get(0).blackUsername());
    }

    @Test
    public void createGameInvalidToken() {
        assertThrows(
                ServerException.class,
                () -> facade.createGame("invalid-token", "My Game")
        );
    }

    @Test
    public void createGameBlankName() throws ServerException {
        var auth = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        assertThrows(
                ServerException.class,
                () -> facade.createGame(auth.authToken(), "   ")
        );

        assertTrue(facade.listGames(auth.authToken()).isEmpty());
    }

    @Test
    public void listGamesEmpty() throws ServerException {
        var auth = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        var games = facade.listGames(auth.authToken());

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    public void listGamesSuccess() throws ServerException {
        var auth = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        int firstID = facade.createGame(auth.authToken(), "First Game");
        int secondID = facade.createGame(auth.authToken(), "Second Game");

        var games = facade.listGames(auth.authToken());

        assertEquals(2, games.size());
        assertNotEquals(firstID, secondID);

        assertTrue(games.stream().anyMatch(game ->
                game.gameID() == firstID
                        && "First Game".equals(game.gameName())
        ));

        assertTrue(games.stream().anyMatch(game ->
                game.gameID() == secondID
                        && "Second Game".equals(game.gameName())
        ));
    }

    @Test
    public void listGamesInvalidToken() {
        assertThrows(
                ServerException.class,
                () -> facade.listGames("invalid-token")
        );
    }

    @Test
    public void listGamesAfterLogout() throws ServerException {
        var auth = facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        facade.logout(auth.authToken());

        assertThrows(
                ServerException.class,
                () -> facade.listGames(auth.authToken())
        );
    }

    @Test
    public void joinGameAsWhite() throws ServerException {
        var auth = facade.register(
                "player1", "password", "player1@email.com"
        );
        int gameID = facade.createGame(auth.authToken(), "My Game");

        facade.joinGame(auth.authToken(), "WHITE", gameID);

        var games = facade.listGames(auth.authToken());

        assertEquals(1, games.size());
        assertEquals(gameID, games.get(0).gameID());
        assertEquals("player1", games.get(0).whiteUsername());
        assertNull(games.get(0).blackUsername());
    }

    @Test
    public void joinGameAsBlack() throws ServerException {
        var auth = facade.register(
                "player1", "password", "player1@email.com"
        );
        int gameID = facade.createGame(auth.authToken(), "My Game");

        facade.joinGame(auth.authToken(), "BLACK", gameID);

        var games = facade.listGames(auth.authToken());

        assertEquals(1, games.size());
        assertEquals("player1", games.get(0).blackUsername());
        assertNull(games.get(0).whiteUsername());
    }

    @Test
    public void joinGameWithTwoPlayers() throws ServerException {
        var white = facade.register(
                "whitePlayer", "password", "white@email.com"
        );
        var black = facade.register(
                "blackPlayer", "password", "black@email.com"
        );
        int gameID = facade.createGame(white.authToken(), "My Game");

        facade.joinGame(white.authToken(), "WHITE", gameID);
        facade.joinGame(black.authToken(), "BLACK", gameID);

        var games = facade.listGames(white.authToken());

        assertEquals(1, games.size());
        assertEquals("whitePlayer", games.get(0).whiteUsername());
        assertEquals("blackPlayer", games.get(0).blackUsername());
    }

    @Test
    public void joinGameOccupiedColor() throws ServerException {
        var first = facade.register(
                "player1", "password", "player1@email.com"
        );
        var second = facade.register(
                "player2", "password", "player2@email.com"
        );
        int gameID = facade.createGame(first.authToken(), "My Game");

        facade.joinGame(first.authToken(), "WHITE", gameID);

        var error = assertThrows(
                ServerException.class,
                () -> facade.joinGame(second.authToken(), "WHITE", gameID)
        );

        assertEquals(
                "That seat is already taken. Choose another color or game.",
                error.getMessage()
        );

        var games = facade.listGames(first.authToken());
        assertEquals("player1", games.get(0).whiteUsername());
        assertNull(games.get(0).blackUsername());
    }

    @Test
    public void joinGameInvalidToken() throws ServerException {
        var auth = facade.register(
                "player1", "password", "player1@email.com"
        );
        int gameID = facade.createGame(auth.authToken(), "My Game");

        assertThrows(
                ServerException.class,
                () -> facade.joinGame("invalid-token", "WHITE", gameID)
        );

        var games = facade.listGames(auth.authToken());

        assertNull(games.get(0).whiteUsername());
        assertNull(games.get(0).blackUsername());
    }

    @Test
    public void joinGameInvalidColor() throws ServerException {
        var auth = facade.register(
                "player1", "password", "player1@email.com"
        );
        int gameID = facade.createGame(auth.authToken(), "My Game");

        assertThrows(
                ServerException.class,
                () -> facade.joinGame(auth.authToken(), "BLUE", gameID)
        );

        var games = facade.listGames(auth.authToken());

        assertNull(games.get(0).whiteUsername());
        assertNull(games.get(0).blackUsername());
    }

    @Test
    public void joinGameDoesNotExist() throws ServerException {
        var auth = facade.register(
                "player1", "password", "player1@email.com"
        );

        assertThrows(
                ServerException.class,
                () -> facade.joinGame(auth.authToken(), "WHITE", -1)
        );
    }
}