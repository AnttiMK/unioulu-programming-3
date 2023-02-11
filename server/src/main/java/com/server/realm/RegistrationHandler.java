package com.server.realm;

import com.server.UserAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class RegistrationHandler implements HttpHandler {

    private final UserAuthenticator auth;

    public RegistrationHandler(UserAuthenticator auth) {
        this.auth = auth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendBadRequest(exchange, "Not supported");
            return;
        }

        String[] credentials = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"))
                .split(":");

        if (credentials.length != 2) {
            sendBadRequest(exchange, "Bad request");
            return;
        }

        if (auth.register(credentials[0], credentials[1])) {
            sendResponse(exchange, 200, "Registered");
        } else {
            sendBadRequest(exchange, "User already exists");
        }
    }

    private void sendBadRequest(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, 400, response);
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

}
