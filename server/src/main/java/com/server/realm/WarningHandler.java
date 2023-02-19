package com.server.realm;

import com.server.WarningMessage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WarningHandler implements HttpHandler {

    private final List<WarningMessage> messageStore = new ArrayList<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("GET")) {
            handleGet(exchange);
        } else if (requestMethod.equals("POST")) {
            handlePost(exchange);
        } else {
            byte[] response = "Not supported".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        if (messageStore.isEmpty()) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            byte[] responseBytes = new JSONArray(messageStore).toString().getBytes(StandardCharsets.UTF_8);
            exchange.setAttribute("content-type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            byte[] response = "Error while parsing JSON".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        try {
            JSONObject json = new JSONObject(requestBody);
            messageStore.add(new WarningMessage(
                    json.getString("nickname"),
                    json.getString("latitude"),
                    json.getString("longitude"),
                    json.getString("dangertype")
            ));
            exchange.sendResponseHeaders(200, -1);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            byte[] response = "Invalid JSON".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }

    }

}
