package dataaccess.sql;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTests {

    private AuthDAO auths;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseSchema.initialize();
        auths = new SQLAuthDAO();
        auths.clear();
    }

    @Test
    void createAuthStoresAllFields() throws DataAccessException {
        AuthData auth = new AuthData("token123", "weston");

        auths.createAuth(auth);

        assertEquals(auth, auths.getAuth("token123"));
    }

    @Test
    void duplicateTokenThrows() throws DataAccessException {
        auths.createAuth(new AuthData("token123", "weston"));

        assertThrows(DataAccessException.class, () ->
                auths.createAuth(new AuthData("token123", "another"))
        );

        assertEquals("weston", auths.getAuth("token123").username());
    }

    @Test
    void getMissingAuthReturnsNull() throws DataAccessException {
        assertNull(auths.getAuth("missing"));
    }

    @Test
    void authPersistsAcrossDAOInstances() throws DataAccessException {
        AuthData auth = new AuthData("token123", "weston");
        auths.createAuth(auth);

        AuthDAO anotherDAO = new SQLAuthDAO();

        assertEquals(auth, anotherDAO.getAuth("token123"));
    }

    @Test
    void deleteAuthRemovesOnlySelectedToken()
            throws DataAccessException {

        auths.createAuth(new AuthData("token123", "weston"));
        AuthData remaining = new AuthData("token456", "another");
        auths.createAuth(remaining);

        auths.deleteAuth("token123");

        assertNull(auths.getAuth("token123"));
        assertEquals(remaining, auths.getAuth("token456"));
    }

    @Test
    void deleteMissingAuthPreservesExistingAuth()
            throws DataAccessException {

        AuthData auth = new AuthData("token123", "weston");
        auths.createAuth(auth);

        auths.deleteAuth("missing");

        assertEquals(auth, auths.getAuth("token123"));
    }

    @Test
    void clearRemovesAllTokens() throws DataAccessException {
        auths.createAuth(new AuthData("token123", "weston"));
        auths.createAuth(new AuthData("token456", "another"));

        auths.clear();

        assertNull(auths.getAuth("token123"));
        assertNull(auths.getAuth("token456"));
    }

    @Test
    void clearEmptyTableSucceeds() {
        assertDoesNotThrow(() -> auths.clear());
    }
}
