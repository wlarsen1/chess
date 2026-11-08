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
        facade.register(
                "player1",
                "password",
                "player1@email.com"
        );

        assertThrows(
                ServerException.class,
                () -> facade.register(
                        "player1",
                        "differentPassword",
                        "different@email.com"
                )
        );
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
}