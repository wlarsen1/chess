package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.Collection;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public int createGame(String authToken, String gameName)
            throws ServiceException, DataAccessException {

        requireAuth(authToken);

        if (gameName == null || gameName.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        return gameDAO.createGame(gameName);
    }

    public Collection<GameData> listGames(String authToken)
            throws ServiceException, DataAccessException {

        requireAuth(authToken);
        return gameDAO.listGames();
    }

    private void requireAuth(String authToken)
            throws ServiceException, DataAccessException {

        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }

    public void joinGame(String authToken, String playerColor, Integer gameID)
            throws ServiceException, DataAccessException {

        requireAuth(authToken);

        if (gameID == null
                || (!"WHITE".equals(playerColor)
                && !"BLACK".equals(playerColor))) {
            throw new ServiceException(400, "Error: bad request");
        }

        GameData game = gameDAO.getGame(gameID);

        if (game == null) {
            throw new ServiceException(400, "Error: bad request");
        }

        String username = authDAO.getAuth(authToken).username();

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if ("WHITE".equals(playerColor)) {
            if (white != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            white = username;
        } else {
            if (black != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            black = username;
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                white,
                black,
                game.gameName(),
                game.game()
        );

        gameDAO.updateGame(updatedGame);
    }
}
