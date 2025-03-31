package edu.ant.myapp;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private String url;
    private String user;
    private String password;

    public DatabaseConnection() {
        loadConfig();
    }

    private void loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("/app/config/config.properties")) {
            properties.load(fis);
            this.url = properties.getProperty("db.url");
            this.user = properties.getProperty("db.user");
            this.password = properties.getProperty("db.password");
            logger.info("Database configuration loaded successfully.");
        } catch (IOException e) {
            logger.error("Error loading database configuration", e);
        }
    }

    public boolean saveData(String username, String bloodGroup, int age) {
        String sql = "INSERT INTO CandidateInfo (username, blood_group, age) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, bloodGroup);
            statement.setInt(3, age);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                logger.info("Successfully inserted record for user: {}", username);
                return true;
            } else {
                logger.warn("Insertion failed for user: {}", username);
                return false;
            }

        } catch (Exception e) {
            logger.error("Database error while inserting user: " + username, e);
            return false;
        }
    }
}
