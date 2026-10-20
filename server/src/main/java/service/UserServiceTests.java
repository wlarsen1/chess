package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private UserDAO users;
    private AuthDAO auths;
    private UserService service;

    @BeforeEach
    public void setUp() {
        users = new MemoryUserDAO();
        auths = new MemoryAuthDAO();
        service = new UserService(users, auths);
    }

    @Test
    public void registerSuccess() throws Exception {
        UserData user = new UserData(
                "weston", "password", "w@email.com"
        );

        AuthData result = service.register(user);

        assertEquals(user, users.getUser("weston"));
        assertEquals("weston", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
        assertEquals(result, auths.getAuth(result.authToken()));
    }

    @Test
    public void registerDuplicateUsername() throws Exception {
        service.register(
                new UserData("weston", "password", "w@email.com")
        );

        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.register(
                        new UserData("weston", "other", "other@email.com")
                )
        );

        assertEquals(403, error.getStatusCode());
        assertEquals("password", users.getUser("weston").password());
    }

    @Test
    public void registerMissingEmail() throws Exception {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.register(
                        new UserData("weston", "password", null)
                )
        );

        assertEquals(400, error.getStatusCode());
        assertNull(users.getUser("weston"));
    }
}
