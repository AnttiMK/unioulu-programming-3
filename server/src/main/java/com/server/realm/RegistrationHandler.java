package com.server.realm;

import com.server.UserAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static com.server.util.ResponseUtil.sendBadRequest;
import static com.server.util.ResponseUtil.sendResponse;

public class RegistrationHandler implements HttpHandler {

    private final UserAuthenticator auth;

    public RegistrationHandler(UserAuthenticator auth) {
        this.auth = auth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            // Only POST is supported
            sendBadRequest(exchange, "Not supported");
            return;
        }

        JSONObject json;
        try {
            json = new JSONObject(new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining()));
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON: " + e.getMessage());
            return;
        }

        String username;
        String password;
        String email;
        try {
            username = json.getString("username");
            password = json.getString("password");
            email = json.getString("email");

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                sendBadRequest(exchange, "All fields were not populated");
                return;
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Missing fields");
            return;
        }

        if (auth.register(username, password, email)) {
            sendResponse(exchange, 200, "Registered");
        } else {
            sendBadRequest(exchange, "User already exists");
        }
    }

}
