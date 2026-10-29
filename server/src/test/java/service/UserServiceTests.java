package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import org.mindrot.jbcrypt.BCrypt;

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

        UserData stored = users.getUser("weston");

        assertNotNull(stored);
        assertEquals(user.username(), stored.username());
        assertEquals(user.email(), stored.email());
        assertNotEquals(user.password(), stored.password());
        assertTrue(BCrypt.checkpw(user.password(), stored.password()));
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

        UserData original = users.getUser("weston");
        assertEquals(403, error.getStatusCode());
        assertEquals(original, users.getUser("weston"));
        assertTrue(BCrypt.checkpw(
                "password", users.getUser("weston").password()
        ));
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

    @Test
    public void loginSuccess() throws Exception {
        AuthData registration = service.register(
                new UserData("weston", "password", "w@email.com")
        );

        AuthData result = service.login(
                new LoginRequest("weston", "password")
        );

        assertEquals("weston", result.username());
        assertNotNull(result.authToken());
        assertNotEquals(registration.authToken(), result.authToken());
        assertEquals(result, auths.getAuth(result.authToken()));
    }

    @Test
    public void loginWrongPassword() throws Exception {
        service.register(
                new UserData("weston", "password", "w@email.com")
        );

        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.login(new LoginRequest("weston", "wrong"))
        );

        assertEquals(401, error.getStatusCode());
    }

    @Test
    public void loginUnknownUser() {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.login(new LoginRequest("unknown", "password"))
        );

        assertEquals(401, error.getStatusCode());
    }

    @Test
    public void loginMissingPassword() {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.login(new LoginRequest("weston", null))
        );

        assertEquals(400, error.getStatusCode());
    }

    @Test
    public void logoutSuccess() throws Exception {
        AuthData registration = service.register(
                new UserData("weston", "password", "w@email.com")
        );

        AuthData secondSession = service.login(
                new LoginRequest("weston", "password")
        );

        service.logout(registration.authToken());

        assertNull(auths.getAuth(registration.authToken()));
        assertNotNull(users.getUser("weston"));
        assertEquals(
                secondSession,
                auths.getAuth(secondSession.authToken())
        );
    }

    @Test
    public void logoutInvalidToken() {
        ServiceException error = assertThrows(
                ServiceException.class,
                () -> service.logout("invalid-token")
        );

        assertEquals(401, error.getStatusCode());
    }
}
