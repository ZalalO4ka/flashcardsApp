module com.example.flashcardsapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.prefs; // Подключаем Jackson

    opens com.example.flashcardsapp to javafx.fxml;
    opens com.example.flashcardsapp.model to com.fasterxml.jackson.databind; // Открываем доступ для JSON

    exports com.example.flashcardsapp;
    exports com.example.flashcardsapp.model;
}