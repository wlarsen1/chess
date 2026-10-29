package server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dataaccess.*;
import dataaccess.sql.SQLAuthDAO;
import dataaccess.sql.SQLGameDAO;
import dataaccess.sql.SQLUserDAO;
import io.javalin.Javalin;
import service.ClearService;
import service.ServiceException;
import service.UserService;
import service.GameService;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        try {
            DatabaseSchema.initialize();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to initialize database", ex);
        }

        javalin = Javalin.create(
                config -> config.staticFiles.add("web")
        );

        Gson gson = new Gson();

        UserDAO users = new SQLUserDAO();
        AuthDAO auths = new SQLAuthDAO();
        GameDAO games = new SQLGameDAO();

        ClearHandler clearHandler = new ClearHandler(
                new ClearService(users, auths, games)
        );

        UserHandler userHandler = new UserHandler(
                new UserService(users, auths)
        );

        GameHandler gameHandler = new GameHandler(
                new GameService(games, auths)
        );

        javalin.exception(ServiceException.class, (error, ctx) -> {
            ctx.status(error.getStatusCode());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(
                    Map.of("message", error.getMessage())
            ));
        });

        javalin.exception(DataAccessException.class, (error, ctx) -> {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(
                    Map.of("message", "Error: storage failure")
            ));
        });

        javalin.exception(JsonParseException.class, (error, ctx) -> {
            ctx.status(400);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(
                    Map.of("message", "Error: bad request")
            ));
        });

        javalin.delete("/db", clearHandler::clear);
        javalin.post("/user", userHandler::register);

        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.post("/game", gameHandler::createGame);
        javalin.get("/game", gameHandler::listGames);

        javalin.put("/game", gameHandler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
