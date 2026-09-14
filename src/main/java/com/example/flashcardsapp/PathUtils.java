package com.example.flashcardsapp;

import java.io.File;

public class PathUtils {
    private static final String APP_DIR_NAME = "MyFlashcardsApp";

    public static File getAppDataDirectory() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File appDir;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            appDir = (appData != null) ? new File(appData, APP_DIR_NAME) : new File(userHome, "AppData\\Roaming\\" + APP_DIR_NAME);
        } else if (os.contains("mac")) {
            appDir = new File(userHome, "Library/Application Support/" + APP_DIR_NAME);
        } else {
            appDir = new File(userHome, ".config/" + APP_DIR_NAME);
        }

        if (!appDir.exists() && !appDir.mkdirs()) {
            System.err.println("Не удалось создать директорию приложения: " + appDir.getAbsolutePath());
        }

        return appDir;
    }

    public static File getDataFile() {
        return new File(getAppDataDirectory(), "data.json");
    }

    public static File getBackupDirectory() {
        File backupDir = new File(getAppDataDirectory(), "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            System.err.println("Не удалось создать директорию бэкапов: " + backupDir.getAbsolutePath());
        }
        return backupDir;
    }
}