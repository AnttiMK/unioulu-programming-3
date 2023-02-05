package com.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server implements HttpHandler {

    private final List<String> postedData = new ArrayList<>();

    private Server() {
    }

    public static void main(String[] args) throws Exception {
        //create the http server to port 8001 with default logger
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        //create context that defines path for the resource, in this case a "help"
        server.createContext("/warning", new Server());
        // creates a default executor
        server.setExecutor(null);
        server.start();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("GET")) {
            handleGet(exchange);
        } else if (requestMethod.equals("POST")) {
            handlePost(exchange);
        } else {
            String response = "Not supported";
            exchange.sendResponseHeaders(400, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        byte[] responseBytes = String.join("\n", postedData).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        postedData.add(requestBody);
        exchange.sendResponseHeaders(200, -1);
        exchange.getResponseBody().close();
    }

}
