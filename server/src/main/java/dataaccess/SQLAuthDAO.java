package dataaccess;

import model.AuthData;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = """
                INSERT INTO auths (authToken, username)
                VALUES (?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = """
                SELECT authToken, username
                FROM auths
                WHERE authToken = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, authToken);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return new AuthData(
                            results.getString("authToken"),
                            results.getString("username")
                    );
                }
            }

            return null;

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get auth", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auths WHERE authToken = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, authToken);
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete auth", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auths";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to clear auths", ex);
        }
    }
}
