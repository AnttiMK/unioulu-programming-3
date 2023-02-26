package com.server.realm;

import com.server.storage.MessageDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class WarningHandler implements HttpHandler {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    private final MessageDatabase database;

    public WarningHandler(MessageDatabase database) {
        this.database = database;
    }

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
        try {
            JSONArray array = database.getMessages();
            if (array == null) {
                exchange.sendResponseHeaders(204, -1);
                exchange.getResponseBody().close();
                return;
            }

            byte[] responseBytes = array.toString().getBytes(StandardCharsets.UTF_8);
            exchange.setAttribute("content-type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.getResponseBody().close();
        } catch (SQLException e) {
            String message = "Error while fetching messages: " + e.getMessage();
            byte[] response = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            exchange.getResponseBody().write(response);
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
            String nickname = json.getString("nickname");
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            long sent = ZonedDateTime.parse(json.getString("sent"), DateTimeFormatter.ofPattern(DATE_PATTERN)).toInstant().toEpochMilli();
            String dangertype = json.getString("dangertype");

            database.handleMessage(nickname, latitude, longitude, sent, dangertype);
            exchange.sendResponseHeaders(200, -1);
            exchange.getResponseBody().close();
        } catch (DateTimeException e) {
            byte[] response = "Invalid date format".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        } catch (SQLException e) {
            String message = "Database error: " + e.getMessage();
            byte[] response = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            byte[] response = "Invalid JSON".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }

    }

}
