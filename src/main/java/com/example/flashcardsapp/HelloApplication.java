package com.example.flashcardsapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Flashcards");

        try {
            var iconStream = getClass().getResourceAsStream("/com/example/flashcardsapp/icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить иконку: " + e.getMessage());
        }

        stage.setScene(scene);

        // Включаем полноэкранный режим (скрывает рамки и панель задач)
        stage.setFullScreen(true);
        // Убираем подсказку "Нажмите ESC для выхода из полноэкранного режима"
        stage.setFullScreenExitHint("");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}