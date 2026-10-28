package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user)
            throws ServiceException, DataAccessException {

        if (user == null
                || user.username() == null
                || user.password() == null
                || user.email() == null
                || user.username().isBlank()
                || user.password().isBlank()
                || user.email().isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        if (userDAO.getUser(user.username()) != null) {
            throw new ServiceException(403, "Error: already taken");
        }

        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        UserData storedUser = new UserData(
                user.username(),
                hashedPassword,
                user.email()
        );

        userDAO.createUser(storedUser);

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());

        authDAO.createAuth(auth);
        return auth;
    }

    public AuthData login(LoginRequest request)
            throws ServiceException, DataAccessException {

        if (request == null
                || request.username() == null
                || request.password() == null
                || request.username().isBlank()
                || request.password().isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        UserData user = userDAO.getUser(request.username());

        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());

        authDAO.createAuth(auth);
        return auth;
    }

    public void logout(String authToken)
            throws ServiceException, DataAccessException {

        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        authDAO.deleteAuth(authToken);
    }
}
