package com.example.flashcardsapp.model;

import com.example.flashcardsapp.PathUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class FileManager {
    private final File dataFile;
    private final ObjectMapper mapper;

    public FileManager() {
        // Берем безопасный путь C:\Users\<User>\AppData\Roaming\MyFlashcardsApp\data.json
        this.dataFile = PathUtils.getDataFile();
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // Сохраняем всю карту учеников в JSON
    public void saveData(Map<String, Student> students) {
        if (students == null) return;

        // Делаем бэкап перед записью
        createBackup();

        try {
            mapper.writeValue(dataFile, students);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Загружаем карту учеников из JSON
    public Map<String, Student> loadData() {
        if (!dataFile.exists()) {
            return null;
        }

        try {
            return mapper.readValue(dataFile, mapper.getTypeFactory().constructMapType(
                    Map.class, String.class, Student.class));
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Авто-создание бэкапа в папке /backups/
    private void createBackup() {
        if (!dataFile.exists()) return;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = new File(PathUtils.getBackupDirectory(), "backup_" + timeStamp + ".json");
            Files.copy(dataFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Ошибка создания резервной копии: " + e.getMessage());
        }
    }

    public boolean exportDeckToCsv(Deck deck, File destinationFile) {
        if (deck == null || deck.getCards() == null) return false;

        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(destinationFile),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            // Записываем BOM для корректного отображения кириллицы в Excel
            writer.write('\ufeff');
            writer.println("Term;Definition"); // Заголовки колонок

            for (Flashcard card : deck.getCards()) {
                // Экранируем кавычки
                String term = (card.getTerm() != null ? card.getTerm() : "").replace("\"", "\"\"");
                String def = (card.getDefinition() != null ? card.getDefinition() : "").replace("\"", "\"\"");
                writer.println(String.format("\"%s\";\"%s\"", term, def));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка при экспорте в CSV: " + e.getMessage());
            return false;
        }
    }
}