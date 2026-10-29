package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String sql = """
                INSERT INTO games (gameName, game)
                VALUES (?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, gameName);
            statement.setString(2, gson.toJson(new ChessGame()));
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new DataAccessException("Failed to get new game ID");

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setInt(1, gameID);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return readGame(results);
                }
            }

            return null;

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get game", ex);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM games";
        Collection<GameData> games = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var results = statement.executeQuery()) {

            while (results.next()) {
                games.add(readGame(results));
            }

            return games;

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to list games", ex);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
                UPDATE games
                SET whiteUsername = ?, blackUsername = ?,
                    gameName = ?, game = ?
                WHERE gameID = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());

            if (statement.executeUpdate() == 0) {
                throw new DataAccessException("Error: game not found");
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update game", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to clear games", ex);
        }
    }

    private GameData readGame(ResultSet results) throws SQLException {
        return new GameData(
                results.getInt("gameID"),
                results.getString("whiteUsername"),
                results.getString("blackUsername"),
                results.getString("gameName"),
                gson.fromJson(results.getString("game"), ChessGame.class)
        );
    }
}
