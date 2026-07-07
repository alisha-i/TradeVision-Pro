package com.tradevision.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class NotificationUtil {

    public static void showToast(Window owner, String message, boolean isSuccess) {
        if (owner == null) return;
        
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        Label label = new Label(message);
        label.setStyle(
            "-fx-background-color: " + (isSuccess ? "#089981" : "#F23645") + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 15px 30px;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);"
        );

        popup.getContent().add(label);

        // Position bottom-right of the window
        popup.setOnShown(e -> {
            popup.setX(owner.getX() + owner.getWidth() - popup.getWidth() - 30);
            popup.setY(owner.getY() + owner.getHeight() - popup.getHeight() - 30);
        });

        popup.show(owner);

        // Animate fade out
        Timeline timeline = new Timeline();
        KeyFrame kf = new KeyFrame(Duration.millis(3000), new KeyValue(popup.opacityProperty(), 0));
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(e -> popup.hide());
        
        // Start animation after 1 second delay
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(timeline::play);
        }).start();
    }
}
