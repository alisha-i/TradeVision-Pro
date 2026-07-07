package com.tradevision.controllers;

import com.tradevision.App;
import com.tradevision.database.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            if (conn == null) {
                showError("Database connection error. Check credentials.");
                return;
            }

            // Check if username or email already exists
            String checkQuery = "SELECT id FROM Users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showError("Username or email already exists.");
                return;
            }

            // Hash password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Insert new user
            String insertQuery = "INSERT INTO Users (username, email, password_hash) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                // Initialize default portfolio using last_insert_rowid()
                try (Statement stmt = conn.createStatement();
                     ResultSet generatedKeys = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        String portfolioQuery = "INSERT INTO Portfolio (user_id, balance) VALUES (?, 100000.00)";
                        PreparedStatement portfolioStmt = conn.prepareStatement(portfolioQuery);
                        portfolioStmt.setInt(1, userId);
                        portfolioStmt.executeUpdate();
                    }
                } // This closes the try-with-resources block!

                errorLabel.setTextFill(javafx.scene.paint.Color.web("#10b981")); // Green
                errorLabel.setText("Registration successful! Please sign in.");
                errorLabel.setVisible(true);
            } else {
                showError("Registration failed. Try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("SQL Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            App.setRoot("views/Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setTextFill(javafx.scene.paint.Color.web("#ef4444")); // Red
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
