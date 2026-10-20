package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTests {

    @Test
    public void clearRemovesAllData() throws DataAccessException {
        UserDAO users = new MemoryUserDAO();
        AuthDAO auths = new MemoryAuthDAO();
        GameDAO games = new MemoryGameDAO();

        users.createUser(new UserData("weston", "password", "w@email.com"));
        auths.createAuth(new AuthData("test-token", "weston"));
        int gameID = games.createGame("Test game");

        ClearService service = new ClearService(users, auths, games);
        service.clear();

        assertNull(users.getUser("weston"));
        assertNull(auths.getAuth("test-token"));
        assertNull(games.getGame(gameID));
        assertTrue(games.listGames().isEmpty());
    }

    @Test
    public void clearEmptyStorageSucceeds() {
        ClearService service = new ClearService(
                new MemoryUserDAO(),
                new MemoryAuthDAO(),
                new MemoryGameDAO()
        );

        assertDoesNotThrow(() -> service.clear());
    }
}
