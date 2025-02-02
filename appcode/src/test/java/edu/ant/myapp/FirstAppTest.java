package edu.ant.myapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Ensure Mockito works with JUnit 5
public class FirstAppTest {

    @Mock
    private DatabaseConnection mockDbConnection;

    @InjectMocks
    private FirstApp firstApp;

    @Test
    public void testSaveToDatabase() {
        // Mock the behavior of the saveData method
        when(mockDbConnection.saveData(anyString(), anyString(), anyInt())).thenReturn(true);

        // Call the method under test
        boolean result = firstApp.saveToDatabase("John", "A+", 25);

        // Assert that the result is true
        assertTrue(result, "Data should be saved successfully.");
    }

    @Test
    public void testSaveToDatabaseWithInvalidData() {
        // Mock the behavior for invalid data
        when(mockDbConnection.saveData(anyString(), anyString(), anyInt())).thenReturn(false);

        // Call the method with invalid data
        boolean result = firstApp.saveToDatabase("", "", -1);  // Invalid input

        // Assert that the result is false
        assertFalse(result, "Data with invalid inputs should not be saved.");
    }
}
