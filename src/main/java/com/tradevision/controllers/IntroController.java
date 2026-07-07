package com.tradevision.controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {

    @FXML
    private WebView webView;
    
    // Store reference to bridge to prevent Garbage Collection
    private final JavaBridge bridge = new JavaBridge();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        
        // Ensure background of webview page doesn't flash white
        webView.setPageFill(javafx.scene.paint.Color.web("#05050A"));
        
        // Add Java Bridge class so JavaScript can call Java methods
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", bridge);
                
                // Show instantly
                webView.setOpacity(1.0);
            }
        });

        // Load intro.html
        String url = getClass().getResource("/com/tradevision/views/intro.html").toExternalForm();
        engine.load(url);
    }

    /**
     * Bridge class must be public and its methods must be public 
     * to be accessible from JavaScript in the WebView.
     */
    public static class JavaBridge {
        public void navigateToLogin() {
            javafx.application.Platform.runLater(() -> {
                try {
                    com.tradevision.App.setRoot("views/Login");
                } catch (Exception e) {
                    System.err.println("Error navigating to Login: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        
        public void navigateToRegister() {
            javafx.application.Platform.runLater(() -> {
                try {
                    com.tradevision.App.setRoot("views/Register");
                } catch (Exception e) {
                    System.err.println("Error navigating to Register: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }
}
