package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

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

        userDAO.createUser(user);

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());

        authDAO.createAuth(auth);
        return auth;
    }
}
