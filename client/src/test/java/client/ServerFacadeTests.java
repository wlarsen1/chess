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
}