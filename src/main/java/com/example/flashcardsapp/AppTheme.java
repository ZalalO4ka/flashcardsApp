package com.example.flashcardsapp;

public enum AppTheme {
    LIGHT("Светлая (Стандартная)",
            "-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);",
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f1f3f5); -fx-border-color: #dee2e6; -fx-border-width: 0 1 0 0;",
            "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa); -fx-border-color: #dee2e6; -fx-border-radius: 16; -fx-background-radius: 16;",
            "-fx-text-fill: #212529;",
            "#d0d7de", // Ярче затухание/выделение на сайдбаре
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); -fx-text-fill: #212529; -fx-border-color: #dee2e6; -fx-border-radius: 10; -fx-background-radius: 10;"),

    DARK("Тёмная",
            "-fx-background-color: linear-gradient(to bottom right, #1e1e2e, #11111b);",
            "-fx-background-color: linear-gradient(to bottom, #181825, #11111b); -fx-border-color: #313244; -fx-border-width: 0 1 0 0;",
            "-fx-background-color: linear-gradient(to bottom right, #2b2b3d, #1e1e2e); -fx-border-color: #45475a; -fx-border-radius: 16; -fx-background-radius: 16;",
            "-fx-text-fill: #cdd6f4;",
            "#585b70", // Сочный контрастный акцент для тёмного сайдбара
            "-fx-background-color: linear-gradient(to bottom, #313244, #2b2b3d); -fx-text-fill: #cdd6f4; -fx-border-color: #45475a; -fx-border-radius: 10; -fx-background-radius: 10;"),

    NORDIC("Скандинавская (Нейтральная)",
            "-fx-background-color: linear-gradient(to bottom right, #e2e8f0, #cbd5e1);",
            "-fx-background-color: linear-gradient(to bottom, #f1f5f9, #e2e8f0); -fx-border-color: #94a3b8; -fx-border-width: 0 1 0 0;",
            "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8fafc); -fx-border-color: #cbd5e1; -fx-border-radius: 16; -fx-background-radius: 16;",
            "-fx-text-fill: #0f172a;",
            "#94a3b8", // Заметное серо-голубое затухание
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f1f5f9); -fx-text-fill: #0f172a; -fx-border-color: #cbd5e1; -fx-border-radius: 10; -fx-background-radius: 10;"),

    SAGE("Шалфей (Нейтральная)",
            "-fx-background-color: linear-gradient(to bottom right, #f1f4f2, #e2e8e4);",
            "-fx-background-color: linear-gradient(to bottom, #f7f9f8, #e8ede9); -fx-border-color: #c2d1c7; -fx-border-width: 0 1 0 0;",
            "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f1f4f2); -fx-border-color: #c2d1c7; -fx-border-radius: 16; -fx-background-radius: 16;",
            "-fx-text-fill: #2d3732;",
            "#a3b899", // Приятный мягко-зеленый акцент
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f1f4f2); -fx-text-fill: #2d3732; -fx-border-color: #c2d1c7; -fx-border-radius: 10; -fx-background-radius: 10;"),

    PASTEL("Пастельная",
            "-fx-background-color: linear-gradient(to bottom right, #fff0f3, #ffe5ec);",
            "-fx-background-color: linear-gradient(to bottom, #ffccd5, #ffb70322); -fx-border-color: #ffb5a7; -fx-border-width: 0 1 0 0;",
            "-fx-background-color: linear-gradient(to bottom right, #ffffff, #fff0f3); -fx-border-color: #ffccd5; -fx-border-radius: 16; -fx-background-radius: 16;",
            "-fx-text-fill: #590d22;",
            "#ffb5a7", // Яркий пастельно-розовый градиентный затенок
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #fff0f3); -fx-text-fill: #590d22; -fx-border-color: #ffccd5; -fx-border-radius: 10; -fx-background-radius: 10;"),

    OCEAN("Океан",
            "-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e3a8a);",
            "-fx-background-color: linear-gradient(to bottom, #1e293b, #0f172a); -fx-border-color: #3b82f6; -fx-border-width: 0 1 0 0;",
            "-fx-background-color: linear-gradient(to bottom right, #1e293b, #1e3a8a); -fx-border-color: #60a5fa; -fx-border-radius: 16; -fx-background-radius: 16;",
            "-fx-text-fill: #f0f9ff;",
            "#3b82f6", // Яркий синий подсвет
            "-fx-background-color: linear-gradient(to bottom, #2563eb, #1d4ed8); -fx-text-fill: #ffffff; -fx-border-color: #60a5fa; -fx-border-radius: 10; -fx-background-radius: 10;");

    private final String name;
    private final String bgStyle;
    private final String sidebarStyle;
    private final String cardStyle;
    private final String textStyle;
    private final String selectionColor;
    private final String dialogButtonStyle;

    AppTheme(String name, String bgStyle, String sidebarStyle, String cardStyle, String textStyle, String selectionColor, String dialogButtonStyle) {
        this.name = name;
        this.bgStyle = bgStyle;
        this.sidebarStyle = sidebarStyle;
        this.cardStyle = cardStyle;
        this.textStyle = textStyle;
        this.selectionColor = selectionColor;
        this.dialogButtonStyle = dialogButtonStyle;
    }

    public String getName() { return name; }
    public String getBgStyle() { return bgStyle; }
    public String getSidebarStyle() { return sidebarStyle; }
    public String getCardStyle() { return cardStyle; }
    public String getTextStyle() { return textStyle; }
    public String getSelectionColor() { return selectionColor; }
    public String getDialogButtonStyle() { return dialogButtonStyle; }

    @Override
    public String toString() { return name; }
}