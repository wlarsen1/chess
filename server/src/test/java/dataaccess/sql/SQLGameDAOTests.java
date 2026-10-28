package dataaccess.sql;

import chess.*;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTests {

    private GameDAO games;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseSchema.initialize();
        games = new SQLGameDAO();
        games.clear();
    }

    @Test
    void createGameStoresInitialState() throws DataAccessException {
        int id = games.createGame("First Game");

        GameData stored = games.getGame(id);

        assertNotNull(stored);
        assertEquals(id, stored.gameID());
        assertEquals("First Game", stored.gameName());
        assertNull(stored.whiteUsername());
        assertNull(stored.blackUsername());
        assertEquals(new ChessGame(), stored.game());
    }

    @Test
    void createGamesGeneratesUniqueIDs() throws DataAccessException {
        int first = games.createGame("First");
        int second = games.createGame("Second");

        assertNotEquals(first, second);
        assertNotNull(games.getGame(first));
        assertNotNull(games.getGame(second));
    }

    @Test
    void createGameWithNullNameThrows() {
        assertThrows(DataAccessException.class, () ->
                games.createGame(null)
        );
    }

    @Test
    void getMissingGameReturnsNull() throws DataAccessException {
        assertNull(games.getGame(-1));
    }

    @Test
    void listGamesReturnsAllGames() throws DataAccessException {
        int first = games.createGame("First");
        int second = games.createGame("Second");

        var listed = games.listGames();

        assertEquals(2, listed.size());
        assertEquals(
                Set.of(first, second),
                listed.stream()
                        .map(GameData::gameID)
                        .collect(Collectors.toSet())
        );
        assertTrue(listed.contains(games.getGame(first)));
        assertTrue(listed.contains(games.getGame(second)));
    }

    @Test
    void listEmptyDatabaseReturnsEmptyCollection()
            throws DataAccessException {

        assertTrue(games.listGames().isEmpty());
    }

    @Test
    void updateGameStoresPlayersAndName() throws DataAccessException {
        int id = games.createGame("Original");
        GameData updated = new GameData(
                id, "weston", "opponent", "Renamed",
                new ChessGame()
        );

        games.updateGame(updated);

        assertEquals(updated, new SQLGameDAO().getGame(id));
    }

    @Test
    void updateGamePersistsMoveAndTurn() throws Exception {
        int id = games.createGame("Move Test");
        ChessGame game = games.getGame(id).game();

        ChessPosition start = new ChessPosition(2, 5);
        ChessPosition end = new ChessPosition(4, 5);
        ChessPiece pawn = game.getBoard().getPiece(start);

        game.makeMove(new ChessMove(start, end, null));

        games.updateGame(new GameData(
                id, "weston", "opponent", "Move Test", game
        ));

        ChessGame loaded = new SQLGameDAO().getGame(id).game();

        assertEquals(game, loaded);
        assertNull(loaded.getBoard().getPiece(start));
        assertEquals(pawn, loaded.getBoard().getPiece(end));
        assertEquals(ChessGame.TeamColor.BLACK, loaded.getTeamTurn());
    }

    @Test
    void updateMissingGameThrows() {
        GameData missing = new GameData(
                -1, null, null, "Missing", new ChessGame()
        );

        assertThrows(DataAccessException.class, () ->
                games.updateGame(missing)
        );
    }

    @Test
    void clearRemovesAllGames() throws DataAccessException {
        int first = games.createGame("First");
        int second = games.createGame("Second");

        games.clear();

        assertTrue(games.listGames().isEmpty());
        assertNull(games.getGame(first));
        assertNull(games.getGame(second));
    }

    @Test
    void clearEmptyTableSucceeds() {
        assertDoesNotThrow(() -> games.clear());
    }
}
