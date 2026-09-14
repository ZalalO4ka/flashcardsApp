package com.example.flashcardsapp.util;

import com.example.flashcardsapp.AppTheme;
import com.example.flashcardsapp.model.Flashcard;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import java.util.Optional;

public class CardDialogHelper {

    public static Optional<Pair<String, String>> showCardDialog(Flashcard cardToEdit, AppTheme currentTheme, Node ownerNode) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        boolean isEdit = (cardToEdit != null);
        dialog.setTitle(isEdit ? "Редактирование карточки" : "Новая карточка");
        dialog.setHeaderText(isEdit ? "Измените термин и определение:" : "Введите термин и определение:");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 20, 10, 10));

        TextField termField = new TextField(isEdit ? cardToEdit.getTerm() : "");
        termField.setPromptText("Термин (Слово)");
        TextField defField = new TextField(isEdit ? cardToEdit.getDefinition() : "");
        defField.setPromptText("Определение (Перевод)");

        Label termLabel = new Label("Термин:");
        Label defLabel = new Label("Определение:");

        Color textColor = (currentTheme == AppTheme.DARK) ? Color.WHITE : Color.web("#1a1a1a");
        termLabel.setTextFill(textColor);
        defLabel.setTextFill(textColor);

        grid.add(termLabel, 0, 0);
        grid.add(termField, 1, 0);
        grid.add(defLabel, 0, 1);
        grid.add(defField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        DialogHelper.styleDialog(dialog, currentTheme, ownerNode);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(termField.getText(), defField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}