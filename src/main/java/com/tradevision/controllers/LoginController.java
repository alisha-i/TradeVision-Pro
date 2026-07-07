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
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        usernameField.setText("alisha_i");
        passwordField.setText("ale@sha07");
        javafx.application.Platform.runLater(() -> handleLogin(new ActionEvent()));
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            if (conn == null) {
                showError("Database connection error. Check credentials.");
                return;
            }

            String query = "SELECT id, password_hash FROM Users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String hash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, hash)) {
                    // Login successful
                    com.tradevision.utils.SessionManager.login(userId, username);
                    errorLabel.setVisible(false);
                    System.out.println("Login successful for user: " + username);
                    
                    try {
                        App.setRoot("views/Dashboard");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        showError("Failed to load Dashboard.");
                    }
                } else {
                    showError("Invalid username or password.");
                }
            } else {
                showError("Invalid username or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("An error occurred during login.");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            App.setRoot("views/Register");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setTextFill(javafx.scene.paint.Color.web("#ef4444")); // Reset to red
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
