package com.server.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for sending HTTP responses.
 */
public class ResponseUtil {

    private ResponseUtil() {
        // Static utility class
    }

    /**
     * Sends an HTTP response to the client.
     *
     * @param exchange The HttpExchange object
     * @param code     The HTTP response code
     * @param response The response body
     * @throws IOException If an I/O error occurs
     */
    public static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    /**
     * Sends a 400 Bad Request response to the client.
     *
     * @param exchange The HttpExchange object
     * @param response The response body
     * @throws IOException If an I/O error occurs
     */
    public static void sendBadRequest(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, 400, response);
    }

    /**
     * Sends a 204 No Content response to the client.
     *
     * @param exchange The HttpExchange object
     * @throws IOException If an I/O error occurs
     */
    public static void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.getResponseBody().close();
    }
}
