package com.example.flashcardsapp;

import com.example.flashcardsapp.model.*;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javafx.stage.FileChooser;
import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

public class HelloController {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private ListView<String> studentListView;
    @FXML private Label sidebarTitle;
    @FXML private MenuButton studentMenuButton;
    @FXML private Label currentStudentTitleLabel;
    @FXML private FlowPane deckGridPane;
    @FXML private StackPane mainContentArea;

    private ProfileManager profileManager = new ProfileManager();
    private FileManager fileManager = new FileManager();
    private Preferences prefs = Preferences.userNodeForPackage(HelloController.class);

    private boolean showingDefinition = false;
    private boolean isAnimating = false;
    private int currentCardIndex = 0;
    private AppTheme currentTheme = AppTheme.LIGHT;
    private Label globalCardTextRef;

    private final String BTNS_STYLE =
            "-fx-background-color: #ffffff; -fx-text-fill: #000000; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: transparent; " +
                    "-fx-background-radius: 10; -fx-padding: 10 20 10 20; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);";

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        applyCssStyles();

        // 3D Камера
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                PerspectiveCamera camera = new PerspectiveCamera(false);
                newScene.setCamera(camera);
            }
        });

        String savedThemeName = prefs.get("app_theme", AppTheme.LIGHT.name());
        try {
            currentTheme = AppTheme.valueOf(savedThemeName);
        } catch (IllegalArgumentException | NullPointerException e) {
            currentTheme = AppTheme.LIGHT;
        }

        profileManager.setStudents(fileManager.loadData());
        studentListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        studentListView.setCellFactory(lv -> new ListCell<>() {
            {
                setOnMouseEntered(e -> {
                    if (!isEmpty() && !isSelected()) {
                        setStyle(getBaseStyle() + " -fx-background-color: rgba(0, 0, 0, 0.05);");
                    }
                });

                setOnMouseExited(e -> {
                    if (!isEmpty() && !isSelected()) {
                        setStyle(getBaseStyle() + " -fx-background-color: transparent;");
                    } else if (!isEmpty() && isSelected()) {
                        setStyle(getBaseStyle() + " -fx-background-color: " + currentTheme.getSelectionColor() + ";");
                    }
                });
            }

            private String getBaseStyle() {
                return "-fx-padding: 10 14 10 14; -fx-background-radius: 8; -fx-cursor: hand; " + currentTheme.getTextStyle();
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setFont(Font.font("System", FontWeight.BOLD, 16));

                    if (isSelected()) {
                        setStyle(getBaseStyle() + " -fx-background-color: " + currentTheme.getSelectionColor() + ";");
                    } else {
                        setStyle(getBaseStyle() + " -fx-background-color: transparent;");
                    }
                }
            }
        });

        studentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                profileManager.setCurrentStudent(newVal);
                updateUI();
            }
        });

        updateStudentList();
    }

    private void applyCssStyles() {
        try {
            var resource = getClass().getResource("/com/example/flashcardsapp/style.css");
            if (resource == null) {
                resource = getClass().getResource("style.css");
            }

            if (resource != null) {
                rootPane.getStylesheets().clear();
                rootPane.getStylesheets().add(resource.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить style.css: " + e.getMessage());
        }
    }

    private void save() {
        fileManager.saveData(profileManager.getStudents());
    }

    private void applyHoverEffect(Node node) {
        node.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), node);
            ft.setToValue(0.75);
            ft.play();
        });
        node.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), node);
            ft.setToValue(1.0);
            ft.play();
        });
    }

    private void styleDialog(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();

        Stage stage = (Stage) pane.getScene().getWindow();

        if (rootPane != null && rootPane.getScene() != null) {
            stage.initOwner(rootPane.getScene().getWindow());
        }

        stage.initStyle(StageStyle.TRANSPARENT);
        pane.getScene().setFill(Color.TRANSPARENT);

        // Привязываем таблицу стилей к сцене диалога, чтобы .menu-item и т.д. работали внутри алертов/диалогов
        try {
            var resource = getClass().getResource("/com/example/flashcardsapp/style.css");
            if (resource == null) {
                resource = getClass().getResource("style.css");
            }
            if (resource != null) {
                pane.getScene().getStylesheets().add(resource.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить style.css для диалога: " + e.getMessage());
        }

        boolean isDark = (currentTheme == AppTheme.DARK);
        String bgColor = isDark ? "#2b2b2b" : "#ffffff";
        String textColor = isDark ? "-fx-text-fill: #ffffff;" : "-fx-text-fill: #1a1a1a;";

        pane.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 16, 0, 0, 4);"
        );

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(12, 16, 4, 16));

        Label titleLabel = new Label(dialog.getTitle() != null ? dialog.getTitle() : "");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setStyle(textColor);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " + textColor);
        closeBtn.setOnAction(e -> stage.close());
        applyHoverEffect(closeBtn);

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
            headerLabel.setStyle(textColor);
            headerLabel.setWrapText(true);
            layout.getChildren().add(headerLabel);
        }

        if (dialog instanceof Alert) {
            String contentText = ((Alert) dialog).getContentText();
            if (contentText != null && !contentText.isEmpty()) {
                Label contentLabel = new Label(contentText);
                contentLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                contentLabel.setStyle(textColor);
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
                applyHoverEffect(btn);
            }
        }
    }

    private void styleMenuButton(MenuButton menuBtn) {
        menuBtn.setStyle("-fx-mark-color: inherit; -fx-background-color: transparent; -fx-cursor: hand;");
    }

    @FXML
    private void toggleSidebar() {
        if (rootPane.getLeft() != null) {
            rootPane.setLeft(null);
        } else {
            rootPane.setLeft(sidebar);
        }
        rootPane.requestFocus();
    }

    private void updateStudentList() {
        studentListView.getItems().clear();
        studentListView.getItems().addAll(profileManager.getStudents().keySet());

        if (profileManager.getCurrentStudent() != null) {
            studentListView.getSelectionModel().select(profileManager.getCurrentStudent().getName());
        } else if (!studentListView.getItems().isEmpty()) {
            studentListView.getSelectionModel().select(0);
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        rootPane.getStyleClass().removeAll("dark-theme", "light-theme", "dark", "light");
        if (currentTheme == AppTheme.DARK) {
            rootPane.getStyleClass().addAll("dark-theme", "dark");
        } else {
            rootPane.getStyleClass().addAll("light-theme", "light");
        }

        rootPane.setStyle(currentTheme.getBgStyle());
        sidebar.setStyle(currentTheme.getSidebarStyle() + " -fx-padding: 16;");

        if (sidebarTitle != null) {
            sidebarTitle.setStyle(currentTheme.getTextStyle());
            sidebarTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
            sidebarTitle.setPadding(new Insets(12, 0, 12, 12));
        }

        if (studentMenuButton != null) {
            studentMenuButton.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            styleMenuButton(studentMenuButton);
        }

        mainContentArea.getChildren().clear();
        mainContentArea.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-viewport-background-color: transparent;");

        VBox contentBox = new VBox(20.0);
        contentBox.setStyle("-fx-padding: 24; -fx-background-color: transparent;");

        HBox header = new HBox(16.0);
        header.setAlignment(Pos.CENTER_LEFT);

        Button menuBtn = new Button("☰");
        menuBtn.setStyle(BTNS_STYLE);
        applyHoverEffect(menuBtn);
        menuBtn.setOnAction(e -> toggleSidebar());

        Student currentStudent = profileManager.getCurrentStudent();
        currentStudentTitleLabel.setText(currentStudent != null ? "Колоды: " + currentStudent.getName() : "Выберите ученика");
        currentStudentTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        currentStudentTitleLabel.setStyle(currentTheme.getTextStyle());

        Button addDeckBtn = new Button("+ Новая колода");
        addDeckBtn.setStyle(BTNS_STYLE);
        applyHoverEffect(addDeckBtn);
        addDeckBtn.setOnAction(e -> onAddDeckClick());

        Button importCsvBtn = new Button("📁 Из CSV");
        importCsvBtn.setStyle(BTNS_STYLE);
        applyHoverEffect(importCsvBtn);
        importCsvBtn.setOnAction(e -> onImportCsvClick());

        Button exportCsvBtn = new Button("📤 В CSV");
        exportCsvBtn.setStyle(BTNS_STYLE);
        applyHoverEffect(exportCsvBtn);
        exportCsvBtn.setOnAction(e -> onExportCsvClicked());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsBtn = new Button("⚙");
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 34px; -fx-cursor: hand; -fx-padding: 0 10 0 10; " + currentTheme.getTextStyle());
        applyHoverEffect(settingsBtn);
        settingsBtn.setOnAction(e -> onOpenSettingsClick());

        Button exitBtn = new Button("✕");
        exitBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 10 0 10; " + currentTheme.getTextStyle());
        applyHoverEffect(exitBtn);
        exitBtn.setOnAction(e -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close(); // или Platform.exit();
        });

// Добавляем exitBtn в конец списка header:
        // Стало:
        header.getChildren().addAll(menuBtn, currentStudentTitleLabel, addDeckBtn, importCsvBtn, exportCsvBtn, spacer, settingsBtn, exitBtn);

        deckGridPane.setStyle("-fx-background-color: transparent;");
        deckGridPane.setHgap(20);
        deckGridPane.setVgap(20);

        contentBox.getChildren().addAll(header, deckGridPane);
        scrollPane.setContent(contentBox);

        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(scrollPane);

        renderDeckGrid();
    }

    private void renderDeckGrid() {
        deckGridPane.getChildren().clear();
        Student student = profileManager.getCurrentStudent();
        if (student == null) return;

        int delayCounter = 0;

        for (Deck deck : student.getDecks().values()) {
            StackPane card = new StackPane();

            card.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.15));
            card.prefHeightProperty().bind(card.prefWidthProperty().multiply(1.25));
            card.minWidthProperty().set(170);
            card.minHeightProperty().set(220);

            card.setStyle(currentTheme.getCardStyle() + " -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

            if (deck.getCoverImagePath() != null && !deck.getCoverImagePath().isEmpty()) {
                try {
                    ImageView coverImageView = new ImageView(new Image(deck.getCoverImagePath()));
                    coverImageView.fitWidthProperty().bind(card.prefWidthProperty());
                    coverImageView.fitHeightProperty().bind(card.prefHeightProperty());
                    coverImageView.setPreserveRatio(false);
                    card.getChildren().add(coverImageView);
                } catch (Exception e) {
                    System.out.println("Ошибка обложки: " + e.getMessage());
                }
            }

            VBox overlay = new VBox(8);
            overlay.setAlignment(Pos.CENTER);
            overlay.setStyle("-fx-background-color: transparent; -fx-padding: 16;");
            overlay.setMaxWidth(Double.MAX_VALUE);

            Label nameLabel = new Label(deck.getName());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            nameLabel.setStyle(currentTheme.getTextStyle());
            nameLabel.setWrapText(true);

            Label countLabel = new Label(deck.getCards().size() + " карточек");
            countLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            countLabel.setStyle(currentTheme.getTextStyle() + " -fx-opacity: 0.7;");

            overlay.getChildren().addAll(nameLabel, countLabel);

            MenuButton menuBtn = new MenuButton("⋮");
            menuBtn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            MenuItem editItem = new MenuItem("✎ Изменить название");
            editItem.setOnAction(e -> onEditDeckClick(deck));

            MenuItem coverItem = new MenuItem("🖼 Изменить обложку");
            coverItem.setOnAction(e -> onChangeDeckCoverClick(deck));

            MenuItem copyItem = new MenuItem("📋 Скопировать ученику");
            copyItem.setOnAction(e -> onCopyDeckClick(deck));

            MenuItem deleteItem = new MenuItem("✕ Удалить");
            deleteItem.setOnAction(e -> onDeleteDeckClick(deck));

            menuBtn.getItems().addAll(editItem, coverItem, copyItem, new SeparatorMenuItem(), deleteItem);
            styleMenuButton(menuBtn);

            StackPane.setAlignment(menuBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(menuBtn, new Insets(8));

            card.getChildren().addAll(overlay, menuBtn);

            card.setOnMouseClicked(e -> {
                if (e.getTarget() instanceof MenuButton || (e.getTarget() instanceof javafx.scene.Node && ((javafx.scene.Node) e.getTarget()).getParent() instanceof MenuButton)) {
                    return;
                }
                openDeckView(deck);
            });

            card.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
                st.setToX(1.03);
                st.setToY(1.03);
                st.play();
            });
            card.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            card.setOpacity(0);
            card.setScaleX(0.8);
            card.setScaleY(0.8);

            FadeTransition ft = new FadeTransition(Duration.millis(250), card);
            ft.setToValue(1.0);

            ScaleTransition st = new ScaleTransition(Duration.millis(250), card);
            st.setToX(1.0);
            st.setToY(1.0);

            ParallelTransition animation = new ParallelTransition(ft, st);
            animation.setDelay(Duration.millis(delayCounter * 40));
            animation.play();

            delayCounter++;
            deckGridPane.getChildren().add(card);
        }
    }

    private boolean startFlipped = false; // Флаг: перевернуты ли карточки по умолчанию (начинать с определения/перевода)

    private void openDeckView(Deck deck) {
        profileManager.setCurrentDeck(deck);
        showingDefinition = startFlipped; // Устанавливаем начальное состояние карточки
        currentCardIndex = 0;

        VBox studyView = new VBox(24);
        studyView.setAlignment(Pos.CENTER);
        studyView.setStyle("-fx-padding: 24; -fx-background-color: transparent;");

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button toggleBtn = new Button("☰");
        toggleBtn.setStyle(BTNS_STYLE);
        toggleBtn.setFocusTraversable(false);
        applyHoverEffect(toggleBtn);
        toggleBtn.setOnAction(e -> {
            toggleSidebar();
            rootPane.requestFocus();
        });

        Button backBtn = new Button("← Назад к колодам");
        backBtn.setStyle(BTNS_STYLE);
        backBtn.setFocusTraversable(false);
        applyHoverEffect(backBtn);
        backBtn.setOnAction(e -> updateUI());

        Label deckTitle = new Label(deck.getName());
        deckTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        deckTitle.setStyle(currentTheme.getTextStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопка переключения стартовой стороны (Перевернуть все карточки)
        Button flipAllBtn = new Button(startFlipped ? "🔄 Показать Термины" : "🔄 Перевернуть все");
        flipAllBtn.setStyle(BTNS_STYLE);
        flipAllBtn.setFocusTraversable(false);
        applyHoverEffect(flipAllBtn);
        flipAllBtn.setOnAction(e -> {
            startFlipped = !startFlipped;
            showingDefinition = startFlipped;
            openDeckView(deck);
            rootPane.requestFocus();
        });

        Button shuffleBtn = new Button("🔀 Перемешать");
        shuffleBtn.setStyle(BTNS_STYLE);
        shuffleBtn.setFocusTraversable(false);
        applyHoverEffect(shuffleBtn);
        shuffleBtn.setOnAction(e -> {
            if (!deck.isEmpty()) {
                Collections.shuffle(deck.getCards());
                currentCardIndex = 0;
                showingDefinition = startFlipped; // Сохраняем настройку при перемешивании
                openDeckView(deck);
                rootPane.requestFocus();
            }
        });

        Button addCardBtn = new Button("+ Добавить карточку");
        addCardBtn.setStyle(BTNS_STYLE);
        addCardBtn.setFocusTraversable(false);
        applyHoverEffect(addCardBtn);
        addCardBtn.setOnAction(e -> {
            showCardDialog(deck, null);
            rootPane.requestFocus();
        });

        Button settingsBtn = new Button("⚙");
        settingsBtn.setFocusTraversable(false);
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 34px; -fx-cursor: hand; -fx-padding: 0 10 0 10; " + currentTheme.getTextStyle());
        applyHoverEffect(settingsBtn);
        settingsBtn.setOnAction(e -> {
            onOpenSettingsClick();
            rootPane.requestFocus();
        });

        Button exitBtn = new Button("✕");
        exitBtn.setFocusTraversable(false);
        exitBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 10 0 10; " + currentTheme.getTextStyle());
        applyHoverEffect(exitBtn);
        exitBtn.setOnAction(e -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();
        });

        topBar.getChildren().addAll(toggleBtn, backBtn, deckTitle, spacer, flipAllBtn, shuffleBtn, addCardBtn, settingsBtn, exitBtn);

        HBox centerBox = new HBox(40);
        centerBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        Button prevArrow = createRoundedArrowButton(false);
        Button nextArrow = createRoundedArrowButton(true);

        prevArrow.setFocusTraversable(false);
        nextArrow.setFocusTraversable(false);

        StackPane flashcardBox = new StackPane();
        flashcardBox.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.45));
        flashcardBox.prefHeightProperty().bind(flashcardBox.prefWidthProperty().multiply(0.5625));

        flashcardBox.maxWidthProperty().bind(flashcardBox.prefWidthProperty());
        flashcardBox.maxHeightProperty().bind(flashcardBox.prefHeightProperty());

        flashcardBox.setMinWidth(440);
        flashcardBox.setMinHeight(247);

        flashcardBox.setStyle(currentTheme.getCardStyle() + " -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 16, 0, 0, 4);");

        Label cardText = new Label();
        cardText.setFont(Font.font("System", FontWeight.BOLD, 28));
        cardText.setWrapText(true);
        cardText.setStyle("-fx-text-alignment: center; " + currentTheme.getTextStyle());
        flashcardBox.getChildren().add(cardText);

        this.globalCardTextRef = cardText;

        VBox bottomActionContainer = new VBox(16);
        bottomActionContainer.setAlignment(Pos.CENTER);

        HBox cardActionBox = new HBox(16);
        cardActionBox.setAlignment(Pos.CENTER);

        Button editCardBtn = new Button("✎ Изменить эту карточку");
        editCardBtn.setStyle(BTNS_STYLE);
        editCardBtn.setFocusTraversable(false);
        applyHoverEffect(editCardBtn);

        Button deleteCardBtn = new Button("✕ Удалить эту карточку");
        deleteCardBtn.setStyle(BTNS_STYLE);
        deleteCardBtn.setFocusTraversable(false);
        applyHoverEffect(deleteCardBtn);

        editCardBtn.setOnAction(e -> {
            if (!deck.isEmpty() && currentCardIndex < deck.getCards().size()) {
                showCardDialog(deck, deck.getCards().get(currentCardIndex));
            }
            rootPane.requestFocus();
        });

        deleteCardBtn.setOnAction(e -> {
            if (!deck.isEmpty() && currentCardIndex < deck.getCards().size()) {
                Flashcard current = deck.getCards().get(currentCardIndex);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Удалить карточку \"" + current.getTerm() + "\"?", ButtonType.YES, ButtonType.NO);
                alert.setTitle("Подтверждение");
                styleDialog(alert);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        deck.getCards().remove(current);
                        save();
                        openDeckView(deck);
                    }
                });
            }
            rootPane.requestFocus();
        });

        cardActionBox.getChildren().addAll(editCardBtn, deleteCardBtn);

        Button restartBtn = new Button("Начать сначала");
        restartBtn.setFocusTraversable(false);
        restartBtn.setStyle("-fx-background-color: linear-gradient(to right, #89f7fe, #66a6ff); " +
                "-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-padding: 14 36 14 36; -fx-background-radius: 30; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 2);");
        applyHoverEffect(restartBtn);
        restartBtn.setVisible(false);

        restartBtn.setOnAction(e -> {
            currentCardIndex = 0;
            showingDefinition = startFlipped;
            openDeckView(deck);
            rootPane.requestFocus();
        });

        bottomActionContainer.getChildren().addAll(cardActionBox, restartBtn);

        Runnable updateText = () -> {
            List<Flashcard> cards = deck.getCards();
            if (cards.isEmpty()) {
                cardText.setText("Колода пуста.\nНажмите '+ Добавить карточку'");
                prevArrow.setDisable(true);
                nextArrow.setDisable(true);
                restartBtn.setVisible(false);
            } else if (currentCardIndex >= cards.size()) {
                cardText.setText("Вы просмотрели все карточки!");
                prevArrow.setDisable(true);
                nextArrow.setDisable(true);
                cardActionBox.setVisible(false);
                restartBtn.setVisible(true);
            } else {
                Flashcard current = cards.get(currentCardIndex);
                cardText.setText(showingDefinition ? current.getDefinition() : current.getTerm());
                prevArrow.setDisable(currentCardIndex == 0);
                nextArrow.setDisable(false);
                cardActionBox.setVisible(true);
                restartBtn.setVisible(false);
            }
        };

        updateText.run();

        Runnable flipCardAction = () -> {
            if (!deck.getCards().isEmpty() && currentCardIndex < deck.getCards().size()) {
                animateFlip(flashcardBox, () -> {
                    showingDefinition = !showingDefinition;
                    updateText.run();
                });
            }
        };

        flashcardBox.setOnMouseClicked(e -> {
            rootPane.requestFocus();
            flipCardAction.run();
        });

        prevArrow.setOnAction(e -> {
            rootPane.requestFocus();
            if (currentCardIndex > 0 && !isAnimating) {
                animateSlide(flashcardBox, false, () -> {
                    currentCardIndex--;
                    showingDefinition = startFlipped; // При переходе назад возвращаем стартовую сторону
                    updateText.run();
                });
            }
        });

        nextArrow.setOnAction(e -> {
            rootPane.requestFocus();
            if (currentCardIndex < deck.getCards().size() && !isAnimating) {
                animateSlide(flashcardBox, true, () -> {
                    currentCardIndex++;
                    showingDefinition = startFlipped; // При переходе вперед возвращаем стартовую сторону
                    updateText.run();
                });
            }
        });

        centerBox.getChildren().addAll(prevArrow, flashcardBox, nextArrow);
        studyView.getChildren().addAll(topBar, centerBox, bottomActionContainer);

        // Настройка клавиатурных сокращений
        rootPane.setFocusTraversable(true);
        rootPane.requestFocus();

        rootPane.setOnKeyPressed(e -> {
            if (isAnimating) return;

            if (e.getCode() == KeyCode.RIGHT) {
                if (!nextArrow.isDisabled()) nextArrow.fire();
                e.consume();
            } else if (e.getCode() == KeyCode.LEFT) {
                if (!prevArrow.isDisabled()) prevArrow.fire();
                e.consume();
            } else if (e.getCode() == KeyCode.SPACE) {
                flipCardAction.run();
                e.consume();
            }
        });

        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(studyView);
    }

    private Button createRoundedArrowButton(boolean isRight) {
        SVGPath path = new SVGPath();
        path.setContent(isRight
                ? "M 10 5 Q 14 5 22 20 Q 25 25 22 30 L 10 45 Q 5 45 5 38 L 5 12 Q 5 5 10 5 Z"
                : "M 25 5 Q 21 5 13 20 Q 10 25 13 30 L 25 45 Q 30 45 30 38 L 30 12 Q 30 5 25 5 Z");
        path.setFill(Color.WHITE);
        path.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.15)));

        Button btn = new Button();
        btn.setGraphic(path);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10;");
        applyHoverEffect(btn);
        return btn;
    }

    private void animateFlip(Node cardNode, Runnable onHalfWay) {
        if (isAnimating) return;
        isAnimating = true;

        // Убедимся, что узел вращается относительно своего центра
        cardNode.setRotationAxis(Rotate.Y_AXIS);

        RotateTransition flipFirst = new RotateTransition(Duration.millis(140), cardNode);
        flipFirst.setAxis(Rotate.Y_AXIS);
        flipFirst.setFromAngle(0);
        flipFirst.setToAngle(90);
        flipFirst.setInterpolator(Interpolator.EASE_IN);

        RotateTransition flipSecond = new RotateTransition(Duration.millis(140), cardNode);
        flipSecond.setAxis(Rotate.Y_AXIS);
        flipSecond.setFromAngle(-90);
        flipSecond.setToAngle(0);
        flipSecond.setInterpolator(Interpolator.EASE_OUT);

        flipFirst.setOnFinished(e -> {
            try {
                onHalfWay.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            flipSecond.play();
        });

        flipSecond.setOnFinished(e -> isAnimating = false);

        flipFirst.play();
    }

    @FXML
    private void onOpenSettingsClick() {
        // 1. Создаем полупрозрачный затемняющий фон на весь экран
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // 2. Создаем карточку настроек
        VBox settingsBox = new VBox(16);
        settingsBox.setMaxSize(360, 220);
        boolean isDark = (currentTheme == AppTheme.DARK);
        settingsBox.setStyle(
                "-fx-background-color: " + (isDark ? "#2b2b2b" : "#ffffff") + ";" +
                        "-fx-background-radius: 16px; -fx-padding: 24; -fx-alignment: center;"
        );

        Label titleLabel = new Label("Настройки оформления");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle(currentTheme.getTextStyle());

//        ChoiceDialog-like ComboBox / ChoiceBox
        ComboBox<AppTheme> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(AppTheme.values());
        themeComboBox.setValue(currentTheme);
        themeComboBox.setStyle("-fx-font-size: 14px;");

        Button applyBtn = new Button("Применить");
        applyBtn.setStyle(BTNS_STYLE);
        applyHoverEffect(applyBtn);

        applyBtn.setOnAction(e -> {
            currentTheme = themeComboBox.getValue();
            prefs.put("app_theme", currentTheme.name());

            // Удаляем оверлей
            mainContentArea.getChildren().remove(overlay);

            studentListView.refresh();
            updateStudentList();
            updateUI();
        });

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; " + currentTheme.getTextStyle());
        closeBtn.setOnAction(e -> mainContentArea.getChildren().remove(overlay));

        HBox topBar = new HBox(titleLabel, new Region(), closeBtn);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);

        settingsBox.getChildren().addAll(topBar, themeComboBox, applyBtn);
        overlay.getChildren().add(settingsBox);

        // 3. Добавляем оверлей поверх основного интерфейса
        mainContentArea.getChildren().add(overlay);
    }
    @FXML
    private void onImportCsvClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Сначала выберите или создайте ученика!");
            alert.setHeaderText(null);
            styleDialog(alert);
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV-файл с карточками");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV файлы", "*.csv"));
        File file = fileChooser.showOpenDialog(mainContentArea.getScene().getWindow());

        if (file != null) {
            String deckName = file.getName().replace(".csv", "");
            Deck newDeck = new Deck(deckName);

            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.contains(";") ? line.split(";") : line.split(",");
                    if (parts.length >= 2) {
                        newDeck.addCard(new Flashcard(parts[0].trim(), parts[1].trim()));
                    }
                }

                current.addDeck(newDeck);
                save();
                updateUI();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Колода \"" + deckName + "\" импортирована! Добавлено карточек: " + newDeck.getCards().size());
                alert.setHeaderText(null);
                styleDialog(alert);
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка чтения CSV: " + e.getMessage());
                alert.setHeaderText(null);
                styleDialog(alert);
                alert.showAndWait();
            }
        }
    }

    @FXML
    protected void onAddStudentClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новый ученик");
        dialog.setHeaderText("Введите имя ученика:");
        ButtonType okButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                if (profileManager.addStudent(trimmed)) {
                    save();
                    updateStudentList();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ученик с таким именем уже существует!");
                    alert.setHeaderText(null);
                    styleDialog(alert);
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    protected void onEditStudentClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) return;

        TextInputDialog dialog = new TextInputDialog(current.getName());
        dialog.setTitle("Редактирование ученика");
        dialog.setHeaderText("Измените имя ученика:");
        ButtonType okButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(newName -> {
            String trimmed = newName.trim();
            if (!trimmed.isEmpty() && !trimmed.equals(current.getName())) {
                if (profileManager.renameStudent(current.getName(), trimmed)) {
                    save();
                    updateStudentList();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ученик с таким именем уже существует!");
                    alert.setHeaderText(null);
                    styleDialog(alert);
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    protected void onDeleteStudentClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText("Удалить колоду \"" + current.getName() + "\"?");

        // 1. Создаем кастомные кнопки с нужными ролями и названиями
        ButtonType deleteButtonType = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        // 2. Применяем их к alert (используем alert вместо dialog)
        alert.getDialogPane().getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        styleDialog(alert);
        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                profileManager.removeStudent(current.getName());
                save();
                updateStudentList();
            }
        });
    }

    @FXML
    protected void onAddDeckClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Сначала выберите или создайте ученика!");
            alert.setHeaderText(null);
            styleDialog(alert);
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новая колода");
        dialog.setHeaderText("Введите название колоды:");
        ButtonType okButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                if (profileManager.addDeckToCurrentStudent(trimmed)) {
                    save();
                    updateUI();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Колода с таким названием уже существует!");
                    alert.setHeaderText(null);
                    styleDialog(alert);
                    alert.showAndWait();
                }
            }
        });
    }

    private void onEditDeckClick(Deck deck) {
        TextInputDialog dialog = new TextInputDialog(deck.getName());
        dialog.setTitle("Редактирование колоды");
        dialog.setHeaderText("Измените название колоды:");

        ButtonType okButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(newName -> {
            String trimmed = newName.trim();
            if (!trimmed.isEmpty() && !trimmed.equals(deck.getName())) {
                if (profileManager.renameDeck(deck, trimmed)) {
                    save();
                    updateUI();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Колода с таким названием уже существует!");
                    alert.setHeaderText(null);
                    styleDialog(alert);
                    alert.showAndWait();
                }
            }
        });
    }

    private void onChangeDeckCoverClick(Deck deck) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите обложку для колоды");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(mainContentArea.getScene().getWindow());
        if (file != null) {
            deck.setCoverImagePath(file.toURI().toString());
            save();
            updateUI();
        }
    }

    private void onCopyDeckClick(Deck deck) {
        List<String> otherStudents = new ArrayList<>(profileManager.getStudents().keySet());
        if (otherStudents.isEmpty()) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>(otherStudents.get(0), otherStudents);
        dialog.setTitle("Скопировать колоду");
        dialog.setHeaderText("Выберите ученика для копирования:");
        styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetStudentName -> {
            Student targetStudent = profileManager.getStudents().get(targetStudentName);
            if (targetStudent != null) {
                if (targetStudent.getDecks().containsKey(deck.getName())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "ошибка. данная колода уже существует");
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    styleDialog(alert);// Убираем дефолтный заголовок внутри контента
                    alert.showAndWait();
                } else {
                    Deck copiedDeck = deck.makeCopy(deck.getName());
                    targetStudent.addDeck(copiedDeck);
                    save();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Успешно скопирована");
                    alert.setTitle("Успех");
                    alert.setHeaderText(null);
                    styleDialog(alert);
                    alert.showAndWait();
                }
            }
        });
    }

    private void onDeleteDeckClick(Deck deck) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText("Удалить колоду \"" + deck.getName() + "\"?");
        ButtonType deleteButtonType = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        styleDialog(alert);
        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                profileManager.getCurrentStudent().removeDeck(deck.getName());
                save();
                updateUI();
            }
        });
    }

    private void showCardDialog(Deck deck, Flashcard cardToEdit) {
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

        boolean isDark = (currentTheme == AppTheme.DARK);
        String labelStyle = isDark ? "-fx-text-fill: #ffffff;" : "-fx-text-fill: #1a1a1a;";

        Label termLabel = new Label("Термин:");
        termLabel.setStyle(labelStyle);
        Label defLabel = new Label("Определение:");
        defLabel.setStyle(labelStyle);

        grid.add(termLabel, 0, 0);
        grid.add(termField, 1, 0);
        grid.add(defLabel, 0, 1);
        grid.add(defField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        styleDialog(dialog);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(termField.getText(), defField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pair -> {
            if (!pair.getKey().trim().isEmpty()) {
                if (isEdit) {
                    cardToEdit.setTerm(pair.getKey().trim());
                    cardToEdit.setDefinition(pair.getValue().trim());
                } else {
                    deck.addCard(new Flashcard(pair.getKey().trim(), pair.getValue().trim()));
                }
                save();
                openDeckView(deck);
                rootPane.requestFocus();
            }
        });
    }

    private static class Pair<K, V> {
        private final K key;
        private final V value;
        public Pair(K key, V value) { this.key = key; this.value = value; }
        public K getKey() { return key; }
        public V getValue() { return value; }
    }

    private void animateSlide(Node cardNode, boolean isNext, Runnable onHalfWay) {
        if (isAnimating) return;
        isAnimating = true;

        double offset = isNext ? -300 : 300; // Направление вылета (влево/вправо)

        // 1. Анимация ухода текущей карточки (сдвиг + затухание)
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(150), cardNode);
        slideOut.setByX(offset);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), cardNode);
        fadeOut.setToValue(0.0);

        ParallelTransition exitAnim = new ParallelTransition(slideOut, fadeOut);

        // 2. Анимация появления новой карточки (приход с противоположной стороны + проявление)
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(150), cardNode);
        slideIn.setFromX(-offset);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), cardNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition enterAnim = new ParallelTransition(slideIn, fadeIn);

        exitAnim.setOnFinished(e -> {
            try {
                onHalfWay.run(); // Меняем текст/данные карточки на середине
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            enterAnim.play();
        });

        enterAnim.setOnFinished(e -> {
            cardNode.setTranslateX(0); // Сбрасываем позицию
            isAnimating = false;
        });

        exitAnim.play();
    }

    @FXML
    private void onExportCsvClicked() {
        Student currentStudent = profileManager.getCurrentStudent();
        if (currentStudent == null || currentStudent.getDecks().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "У текущего ученика нет доступных колод!");
            styleDialog(alert);
            alert.showAndWait();
            return;
        }

        // Если колода одна — выбираем её сразу, если несколько — показываем выбор
        List<String> deckNames = new ArrayList<>(currentStudent.getDecks().keySet());
        if (deckNames.size() == 1) {
            exportDeckToCsvFile(currentStudent.getDecks().get(deckNames.get(0)));
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(deckNames.get(0), deckNames);
        dialog.setTitle("Экспорт колоды");
        dialog.setHeaderText("Выберите колоду для экспорта в CSV:");
        styleDialog(dialog);

        dialog.showAndWait().ifPresent(selectedDeckName -> {
            Deck deckToExport = currentStudent.getDecks().get(selectedDeckName);
            if (deckToExport != null) {
                exportDeckToCsvFile(deckToExport);
            }
        });
    }

    // Вспомогательный метод экспорта файла
    private void exportDeckToCsvFile(Deck deck) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить колоду в CSV");
        fileChooser.setInitialFileName(deck.getName() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv")
        );

        File file = fileChooser.showSaveDialog(mainContentArea.getScene().getWindow());
        if (file != null) {
            boolean success = fileManager.exportDeckToCsv(deck, file);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Колода \"" + deck.getName() + "\" успешно экспортирована!");
                styleDialog(alert);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось экспортировать файл.");
                styleDialog(alert);
                alert.showAndWait();
            }
        }
    }
}