package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private GameDAO games;
    private GameService service;

    @BeforeEach
    public void setUp() throws DataAccessException {
        games = new MemoryGameDAO();
        AuthDAO auths = new MemoryAuthDAO();

        auths.createAuth(new AuthData("valid-token", "weston"));
        service = new GameService(games, auths);
    }

    @Test
    public void createGameSuccess() throws Exception {
        int gameID = service.createGame("valid-token", "My game");

        GameData game = games.getGame(gameID);

        assertNotNull(game);
        assertEquals("My game", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    public void createGameUnauthorized() throws Exception {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.createGame("invalid-token", "My game")
        );

        assertEquals(401, error.getStatusCode());
        assertTrue(games.listGames().isEmpty());
    }

    @Test
    public void createGameMissingName() throws Exception {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.createGame("valid-token", null)
        );

        assertEquals(400, error.getStatusCode());
        assertTrue(games.listGames().isEmpty());
    }

    @Test
    public void listGamesSuccess() throws Exception {
        int firstID = games.createGame("First game");
        int secondID = games.createGame("Second game");

        Collection<GameData> result = service.listGames("valid-token");

        assertEquals(2, result.size());
        assertTrue(result.contains(games.getGame(firstID)));
        assertTrue(result.contains(games.getGame(secondID)));
    }

    @Test
    public void listGamesEmpty() throws Exception {
        assertTrue(service.listGames("valid-token").isEmpty());
    }

    @Test
    public void listGamesUnauthorized() {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.listGames("invalid-token")
        );

        assertEquals(401, error.getStatusCode());
    }
}
