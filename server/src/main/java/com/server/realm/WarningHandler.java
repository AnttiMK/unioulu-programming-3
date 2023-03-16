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
            sendBadRequest(exchange, "Not supported");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            JSONArray array = database.getMessages();
            if (array.isEmpty()) {
                exchange.sendResponseHeaders(204, -1);
                exchange.getResponseBody().close();
                return;
            }
            sendJSONResponse(exchange, array.toString().getBytes());
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Error while fetching messages: " + e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error while parsing JSON");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining()));
            if (json.has("query")) {
                handleQuery(exchange, json);
            } else { // Assume regular message
                handleMessage(exchange, json);
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON");
        }

    }

    private void handleQuery(HttpExchange exchange, JSONObject json) throws IOException {
        String queryType = json.getString("query");
        if ("user".equals(queryType) && json.has("nickname")) {

        } else if ("time".equals(queryType) && json.has("timestart") && json.has("timeend")) {

        } else {
            sendBadRequest(exchange, "Invalid query type or missing parameters");
        }
    }

    private void handleMessage(HttpExchange exchange, JSONObject json) throws IOException, SQLException {
        try {
            String nickname = json.getString("nickname");
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            long sent = ZonedDateTime.parse(json.getString("sent"), DateTimeFormatter.ofPattern(DATE_PATTERN)).toInstant().toEpochMilli();
            String dangerType = json.getString("dangertype");
            if (!dangerType.equals("Deer") && !dangerType.equals("Reindeer") && !dangerType.equals("Moose") && !dangerType.equals("Other")) {
                sendBadRequest(exchange, "Invalid dangertype");
                return;
            }

            String areaCode = null;
            String phoneNumber = null;
            if (json.has("areacode")) {
                areaCode = json.getString("areacode");
            }
            if (json.has("phonenumber")) {
                phoneNumber = json.getString("phonenumber");
            }

            database.handleMessage(nickname, latitude, longitude, sent, dangerType, areaCode, phoneNumber);
            exchange.sendResponseHeaders(200, -1);
            exchange.getResponseBody().close();
        } catch (DateTimeException e) {
            sendBadRequest(exchange, "Invalid date format");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
            String message = "Database error: " + e.getMessage();
            sendResponse(exchange, 500, message);
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON");
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

    private void sendJSONResponse(HttpExchange exchange, byte[] json) throws IOException {
        exchange.setAttribute("content-type", "application/json");
        exchange.sendResponseHeaders(200, json.length);
        exchange.getResponseBody().write(json);
        exchange.getResponseBody().close();
    }

}
