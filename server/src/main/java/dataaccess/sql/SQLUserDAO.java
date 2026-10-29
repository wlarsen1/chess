package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = """
                SELECT username, password, email
                FROM users
                WHERE username = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return new UserData(
                            results.getString("username"),
                            results.getString("password"),
                            results.getString("email")
                    );
                }
            }

            return null;

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get user", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to clear users", ex);
        }
    }
}
