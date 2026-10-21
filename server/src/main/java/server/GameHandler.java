package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import request.CreateGameRequest;
import service.GameService;
import service.ServiceException;
import request.JoinGameRequest;

import java.util.Map;

public class GameHandler {

    private final GameService service;
    private final Gson gson = new Gson();

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void createGame(Context ctx)
            throws ServiceException, DataAccessException {

        String authToken = ctx.header("authorization");

        CreateGameRequest request = gson.fromJson(
                ctx.body(), CreateGameRequest.class
        );

        String gameName = request == null ? null : request.gameName();
        int gameID = service.createGame(authToken, gameName);

        ctx.contentType("application/json");
        ctx.result(gson.toJson(Map.of("gameID", gameID)));
    }

    public void listGames(Context ctx)
            throws ServiceException, DataAccessException {

        String authToken = ctx.header("authorization");
        var games = service.listGames(authToken);

        ctx.contentType("application/json");
        ctx.result(gson.toJson(Map.of("games", games)));
    }

    public void joinGame(Context ctx)
            throws ServiceException, DataAccessException {

        String authToken = ctx.header("authorization");

        JoinGameRequest request = gson.fromJson(
                ctx.body(), JoinGameRequest.class
        );

        String color = request == null ? null : request.playerColor();
        Integer gameID = request == null ? null : request.gameID();

        service.joinGame(authToken, color, gameID);

        ctx.contentType("application/json");
        ctx.result("{}");
    }
}
