package edu.ant.myapp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class FirstApp {

    private static final Logger logger = LoggerFactory.getLogger(FirstApp.class);
    private DatabaseConnection dbConnection;

    // Constructor for dependency injection
    public FirstApp(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // Public method to save data to the database
    public boolean saveToDatabase(String username, String bloodGroup, int age) {
        return dbConnection.saveData(username, bloodGroup, age);
    }

    // HTTP handler for /saveData endpoint
    private static class SaveDataHandler implements HttpHandler {
        private final FirstApp app;

        public SaveDataHandler(FirstApp app) {
            this.app = app;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.info("Received request: " + exchange.getRequestMethod());

            // Allow CORS
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            // Handle OPTIONS request
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            // Handle only POST requests
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> formData = parseFormData(exchange);
                String username = formData.get("username");
                String bloodGroup = formData.get("bloodGroup");
                int age = Integer.parseInt(formData.get("age"));

                if (username == null || bloodGroup == null || age <= 0) {
                    sendResponse(exchange, 400, "Invalid input data");
                    return;
                }

                boolean result = app.saveToDatabase(username, bloodGroup, age);
                sendResponse(exchange, 200, result ? "Data saved successfully!" : "Error saving data.");
            } else {
                sendResponse(exchange, 405, "Invalid request method.");
            }
        }

        // Helper method to parse form data
        private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
            Map<String, String> formData = new HashMap<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] params = line.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        formData.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return formData;
        }

        // Helper method to send response
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // Main method - entry point
    public static void main(String[] args) throws IOException {
        int port = 8080;
        logger.info("Starting server on port: " + port);

        DatabaseConnection dbConnection = new DatabaseConnection();
        FirstApp app = new FirstApp(dbConnection);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/saveData", new SaveDataHandler(app));
        server.start();

        logger.info("Server started. Listening on port: " + port);
    }
}
