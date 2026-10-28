package dataaccess;

import java.sql.SQLException;

public class DatabaseSchema {

    public static void initialize() throws DataAccessException {
        DatabaseManager.createDatabase();

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS auths (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT PRIMARY KEY AUTO_INCREMENT,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game JSON NOT NULL
                )
                """
        };

        try (var connection = DatabaseManager.getConnection()) {
            for (String sql : statements) {
                try (var statement = connection.prepareStatement(sql)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create tables", ex);
        }
    }
}
