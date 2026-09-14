package com.example.flashcardsapp.util;

import com.example.flashcardsapp.AppTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DialogHelper {

    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void styleDialog(Dialog<?> dialog, AppTheme currentTheme, Node rootNode) {
        DialogPane pane = dialog.getDialogPane();
        Stage stage = (Stage) pane.getScene().getWindow();

        if (rootNode != null && rootNode.getScene() != null) {
            stage.initOwner(rootNode.getScene().getWindow());
        }

        stage.initStyle(StageStyle.TRANSPARENT);
        pane.getScene().setFill(Color.TRANSPARENT);

        try {
            var resource = DialogHelper.class.getResource("/com/example/flashcardsapp/style.css");
            if (resource != null) {
                pane.getScene().getStylesheets().add(resource.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Ошибка стилей диалога: " + e.getMessage());
        }

        boolean isDark = (currentTheme == AppTheme.DARK);
        String bgColor = isDark ? "#2b2b2b" : "#ffffff";
        Color labelColor = isDark ? Color.WHITE : Color.web("#1a1a1a");

        pane.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 16px; -fx-border-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 16, 0, 0, 4);"
        );

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(12, 16, 4, 16));

        Label titleLabel = new Label(dialog.getTitle() != null ? dialog.getTitle() : "");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setTextFill(labelColor);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setTextFill(labelColor);
        closeBtn.setOnAction(e -> stage.close());
        AnimationUtils.applyHoverEffect(closeBtn);

        titleBar.getChildren().addAll(titleLabel, titleSpacer, closeBtn);

        titleBar.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(0, 20, 20, 20));

        if (pane.getHeaderText() != null && !pane.getHeaderText().isEmpty()) {
            Label headerLabel = new Label(pane.getHeaderText());
            headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            headerLabel.setTextFill(labelColor);
            headerLabel.setWrapText(true);
            layout.getChildren().add(headerLabel);
        }

        if (dialog instanceof Alert alert) {
            String contentText = alert.getContentText();
            if (contentText != null && !contentText.isEmpty()) {
                Label contentLabel = new Label(contentText);
                contentLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                contentLabel.setTextFill(labelColor);
                contentLabel.setWrapText(true);
                layout.getChildren().add(contentLabel);
            }
        }

        if (pane.getContent() != null) {
            layout.getChildren().add(pane.getContent());
        }

        pane.setHeader(titleBar);
        pane.setContent(layout);

        for (ButtonType type : pane.getButtonTypes()) {
            Button btn = (Button) pane.lookupButton(type);
            if (btn != null) {
                btn.setStyle(currentTheme.getDialogButtonStyle() + " -fx-background-radius: 12; -fx-font-weight: bold;");
                AnimationUtils.applyHoverEffect(btn);
            }
        }
    }
}