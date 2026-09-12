package com.example.flashcardsapp;

import java.io.File;

public class PathUtils {
    private static final String APP_DIR_NAME = "MyFlashcardsApp"; // Название твоей программы

    public static File getAppDataDirectory() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File appDir;

        if (os.contains("win")) {
            // Windows: C:\Users\<User>\AppData\Roaming\MyFlashcardsApp
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                appDir = new File(appData, APP_DIR_NAME);
            } else {
                appDir = new File(userHome, "AppData\\Roaming\\" + APP_DIR_NAME);
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/MyFlashcardsApp
            appDir = new File(userHome, "Library/Application Support/" + APP_DIR_NAME);
        } else {
            // Linux/Unix: ~/.config/MyFlashcardsApp
            appDir = new File(userHome, ".config/" + APP_DIR_NAME);
        }

        if (!appDir.exists()) {
            appDir.mkdirs(); // Создаем папку, если её ещё нет
        }

        return appDir;
    }

    public static File getDataFile() {
        return new File(getAppDataDirectory(), "data.json");
    }

    public static File getBackupDirectory() {
        File backupDir = new File(getAppDataDirectory(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }
}