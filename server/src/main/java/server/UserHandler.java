package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.ServiceException;
import service.UserService;

public class UserHandler {

    private final UserService service;
    private final Gson gson = new Gson();

    public UserHandler(UserService service) {
        this.service = service;
    }

    public void register(Context ctx)
            throws ServiceException, DataAccessException {

        UserData user = gson.fromJson(ctx.body(), UserData.class);
        AuthData result = service.register(user);

        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
}
