package edu.ant.myapp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class FirstApp {

    private DatabaseConnection dbConnection;

    // Constructor for dependency injection
    public FirstApp(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // Public method to save data to the database
    public boolean saveToDatabase(String username, String bloodGroup, int age) {
        return dbConnection.saveData(username, bloodGroup, age);
    }

    // HTTP handler for incoming requests
    private static class SaveDataHandler implements HttpHandler {
        private final FirstApp app;

        public SaveDataHandler(FirstApp app) {
            this.app = app;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS headers for every request
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            // Handle preflight OPTIONS request
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1); // No body for OPTIONS request
                return;
            }

            // Handle POST request
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Parse form data
                Map<String, String> formData = parseFormData(exchange);

                String username = formData.get("username");
                String bloodGroup = formData.get("bloodGroup");
                int age = Integer.parseInt(formData.get("age"));

                // Validate input
                if (username == null || bloodGroup == null || age <= 0) {
                    String response = "Invalid or missing parameters.";
                    exchange.sendResponseHeaders(400, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    return;
                }

                // Save data
                boolean result = app.saveToDatabase(username, bloodGroup, age);
                String response = result ? "Data saved successfully!" : "Error saving data.";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                String response = "Invalid request method.";
                exchange.sendResponseHeaders(405, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        // Helper method to parse form data
        private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
            Map<String, String> formData = new HashMap<>();

            // Read the form data from the request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            // Split the data into key-value pairs
            String[] params = sb.toString().split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    formData.put(keyValue[0], keyValue[1]);
                }
            }
            return formData;
        }
    }

    // Main method - entry point of the application
    public static void main(String[] args) throws IOException {
        // Default port
        int port = 8080;

        // Check for port override via command-line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number provided. Using default port 8080.");
            }
        }

        System.out.println("Starting server on port: " + port);

        // Create a DatabaseConnection instance
        DatabaseConnection dbConnection = new DatabaseConnection();

        // Inject the DatabaseConnection into FirstApp
        FirstApp app = new FirstApp(dbConnection);

        // Create an HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/saveData", new SaveDataHandler(app));

        // Start the server
        server.start();
        System.out.println("Server started. Listening on port: " + port);
    }
}
