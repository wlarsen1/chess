package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseSchema;
import dataaccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {

    private UserDAO users;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseSchema.initialize();
        users = new SQLUserDAO();
        users.clear();
    }

    @Test
    void createUserStoresAllFields() throws DataAccessException {
        UserData user = new UserData(
                "weston", "storedPasswordHash", "weston@example.com"
        );

        users.createUser(user);

        assertEquals(user, users.getUser("weston"));
    }

    @Test
    void createDuplicateUserThrows() throws DataAccessException {
        users.createUser(new UserData(
                "weston", "firstHash", "first@example.com"
        ));

        assertThrows(DataAccessException.class, () ->
                users.createUser(new UserData(
                        "weston", "secondHash", "second@example.com"
                ))
        );

        assertEquals("firstHash", users.getUser("weston").password());
    }

    @Test
    void getMissingUserReturnsNull() throws DataAccessException {
        assertNull(users.getUser("missing"));
    }

    @Test
    void userPersistsAcrossDAOInstances() throws DataAccessException {
        UserData user = new UserData(
                "weston", "storedPasswordHash", "weston@example.com"
        );
        users.createUser(user);

        UserDAO anotherDAO = new SQLUserDAO();

        assertEquals(user, anotherDAO.getUser("weston"));
    }

    @Test
    void clearRemovesAllUsers() throws DataAccessException {
        users.createUser(new UserData(
                "weston", "firstHash", "weston@example.com"
        ));
        users.createUser(new UserData(
                "another", "secondHash", "another@example.com"
        ));

        users.clear();

        assertNull(users.getUser("weston"));
        assertNull(users.getUser("another"));
    }

    @Test
    void clearEmptyTableSucceeds() {
        assertDoesNotThrow(() -> users.clear());
    }
}
