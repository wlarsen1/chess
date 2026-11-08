package client;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import model.AuthData;
import model.UserData;
import request.LoginRequest;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535."
            );
        }

        serverUrl = "http://localhost:" + port;

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void clear() throws ServerException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    public AuthData register(
            String username,
            String password,
            String email
    ) throws ServerException {

        UserData request = new UserData(username, password, email);

        return makeRequest(
                "POST",
                "/user",
                request,
                null,
                AuthData.class
        );
    }

    public AuthData login(
            String username,
            String password
    ) throws ServerException {

        LoginRequest request = new LoginRequest(username, password);

        return makeRequest(
                "POST",
                "/session",
                request,
                null,
                AuthData.class
        );
    }

    public void logout(String authToken) throws ServerException {
        makeRequest(
                "DELETE",
                "/session",
                null,
                authToken,
                null
        );
    }

    private <T> T makeRequest(
            String method,
            String path,
            Object requestBody,
            String authToken,
            Class<T> responseType
    ) throws ServerException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .timeout(Duration.ofSeconds(10));

        if (authToken != null) {
            builder.header("Authorization", authToken);
        }

        HttpRequest.BodyPublisher body =
                HttpRequest.BodyPublishers.noBody();

        if (requestBody != null) {
            builder.header("Content-Type", "application/json");
            body = HttpRequest.BodyPublishers.ofString(
                    gson.toJson(requestBody)
            );
        }

        HttpRequest request = builder.method(method, body).build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new ServerException(
                        errorMessage(response.statusCode())
                );
            }

            if (responseType == null) {
                return null;
            }

            T result = gson.fromJson(response.body(), responseType);

            if (result == null) {
                throw new ServerException(
                        "The server returned an unexpected response."
                );
            }

            return result;

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServerException("The request was interrupted.");

        } catch (IOException ex) {
            throw new ServerException(
                    "Unable to reach the server. Please try again."
            );

        } catch (JsonParseException ex) {
            throw new ServerException(
                    "The server returned an unexpected response."
            );
        }
    }

    private String errorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Please check the information you entered.";
            case 401 -> "Please check your credentials or log in again.";
            case 403 -> "That action is not allowed.";
            case 404 -> "The requested item could not be found.";
            case 409 -> "That username is already taken.";
            default -> "The server could not complete your request.";
        };
    }
}
