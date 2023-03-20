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
import java.util.List;
import java.util.stream.Collectors;

public class WarningHandler implements HttpHandler {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    private static final List<String> DANGER_TYPES = List.of("Deer", "Reindeer", "Moose", "Other");
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

    /**
     * Handles a GET request to fetch all messages.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            JSONArray array = database.getMessages();
            if (array.isEmpty()) {
                sendNoContent(exchange);
                return;
            }
            sendJSONResponse(exchange, array.toString().getBytes());
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Error while fetching messages: " + e.getMessage());
        }
    }

    /**
     * Handles an initial POST request, and delegates to the appropriate handler.
     *
     * @param exchange The HttpExchange object
     * @throws IOException If an I/O error occurs
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining()));
            if (json.has("query")) {
                handleQuery(exchange, json);
            } else { // Assume regular / update message
                handleMessage(exchange, json);
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Invalid JSON");
        }

    }

    /**
     * Handles a message query (GET) request.
     */
    private void handleQuery(HttpExchange exchange, JSONObject json) throws IOException {
        String queryType = json.getString("query");
        if ("user".equals(queryType) && json.has("nickname")) {
            // Query by nickname
            try {
                String nickname = json.getString("nickname");
                JSONArray array = database.getMessages(nickname);
                if (array.isEmpty()) {
                    sendNoContent(exchange);
                    return;
                }
                sendJSONResponse(exchange, array.toString().getBytes());
            } catch (SQLException e) {
                sendResponse(exchange, 500, "Error while fetching messages: " + e.getMessage());
            }
        } else if ("time".equals(queryType) && json.has("timestart") && json.has("timeend")) {
            // Query by time
            try {
                long timeStart = dateStringToEpochMilli(json.getString("timestart"));
                long timeEnd = dateStringToEpochMilli(json.getString("timeend"));
                JSONArray array = database.getMessages(timeStart, timeEnd);
                if (array.isEmpty()) {
                    sendNoContent(exchange);
                }
                sendJSONResponse(exchange, array.toString().getBytes());
            } catch (DateTimeException e) {
                sendBadRequest(exchange, "Invalid date format");
            } catch (SQLException e) {
                sendResponse(exchange, 500, "Error while fetching messages: " + e.getMessage());
            }
        } else {
            sendBadRequest(exchange, "Invalid query type or missing parameters");
        }
    }

    /**
     * Sends a JSON response to the client.
     *
     * @param exchange The HttpExchange object
     * @param json     The JSON response
     * @throws IOException If an I/O error occurs
     */
    private void sendJSONResponse(HttpExchange exchange, byte[] json) throws IOException {
        exchange.setAttribute("content-type", "application/json");
        exchange.sendResponseHeaders(200, json.length);
        exchange.getResponseBody().write(json);
        exchange.getResponseBody().close();
    }

    /**
     * Handles a POST request to submit or update a message.
     */
    private void handleMessage(HttpExchange exchange, JSONObject json) throws IOException, SQLException {
        // Check if the request is a submission or an update
        boolean isUpdate = json.has("id");
        try {
            String nickname = json.getString("nickname");
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            long sent = dateStringToEpochMilli(json.getString("sent"));
            String dangerType = json.getString("dangertype");
            String username = exchange.getPrincipal().getUsername();
            if (!DANGER_TYPES.contains(dangerType)) {
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

            /*
             * If the request is an update, check if the user is the sender of the message.
             * If is, update the message. Otherwise, send a 403 Forbidden response.
             */
            if (isUpdate) {
                int id = json.getInt("id");
                if (!database.isSender(id, username)) {
                    sendResponse(exchange, 403, "You are not the sender of this message!");
                    return;
                }
                String updateReason = json.getString("updatereason");
                database.updateMessage(id, nickname, latitude, longitude, sent, dangerType, areaCode, phoneNumber, updateReason, System.currentTimeMillis());
            } else {
                database.submitMessage(nickname, latitude, longitude, sent, dangerType, areaCode, phoneNumber, username);
            }
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
            System.out.println("Invalid JSON: " + e.getMessage());
            sendBadRequest(exchange, "Invalid JSON");
        }
    }

    /**
     * Converts date string with a predefined format to epoch milliseconds.
     *
     * @param date The date string
     * @return The epoch milliseconds
     * @throws DateTimeException If the date string is invalid or is not in the correct format
     */
    private long dateStringToEpochMilli(String date) throws DateTimeException {
        return ZonedDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN)).toInstant().toEpochMilli();
    }

    /**
     * Sends a 400 Bad Request response to the client.
     *
     * @param exchange The HttpExchange object
     * @param response The response body
     * @throws IOException If an I/O error occurs
     */
    private void sendBadRequest(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, 400, response);
    }

    /**
     * Sends a 204 No Content response to the client.
     *
     * @param exchange The HttpExchange object
     * @throws IOException If an I/O error occurs
     */
    private void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.getResponseBody().close();
    }

    /**
     * Sends a HTTP response to the client.
     *
     * @param exchange The HttpExchange object
     * @param code     The HTTP response code
     * @param response The response body
     * @throws IOException If an I/O error occurs
     */
    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

}
