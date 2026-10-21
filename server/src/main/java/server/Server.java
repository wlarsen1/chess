package server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dataaccess.*;
import io.javalin.Javalin;
import service.ClearService;
import service.ServiceException;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(
                config -> config.staticFiles.add("web")
        );

        Gson gson = new Gson();

        UserDAO users = new MemoryUserDAO();
        AuthDAO auths = new MemoryAuthDAO();
        GameDAO games = new MemoryGameDAO();

        ClearHandler clearHandler = new ClearHandler(
                new ClearService(users, auths, games)
        );

        UserHandler userHandler = new UserHandler(
                new UserService(users, auths)
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
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
