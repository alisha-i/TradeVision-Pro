package com.tradevision;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.net.URL;
import java.io.File;

public class Test {
    public static void main(String[] args) {
        javafx.application.Platform.startup(() -> {
            try {
                URL url = new File("src/main/resources/com/tradevision/views/Dashboard.fxml").toURI().toURL();
                System.out.println("Loading URL: " + url);
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                System.out.println("SUCCESSFULLY LOADED!");
            } catch (Exception e) {
                System.out.println("ERROR LOADING FXML:");
                e.printStackTrace();
            }
            System.exit(0);
        });
    }
}
