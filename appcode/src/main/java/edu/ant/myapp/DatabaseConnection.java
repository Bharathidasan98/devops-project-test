package edu.ant.myapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DatabaseConnection {

    // Database connection parameters
    private static final String URL = "jdbc:mysql://bharathitest.mysql.database.azure.com:3306/CandidateDB?useSSL=true&requireSSL=true&verifyServerCertificate=false";
    private static final String USER = "adminbf";
    private static final String PASSWORD = "Nimal!4321";

    // Method to save data into the database
    public boolean saveData(String username, String bloodGroup, int age) {
        String sql = "INSERT INTO CandidateInfo (username, blood_group, age) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, bloodGroup);
            statement.setInt(3, age);

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;  // Return true if data was inserted
        } catch (Exception e) {
            e.printStackTrace();  // Print the exception if there’s an error
            return false;  // Return false if there’s an error
        }
    }
}
