package com.example.flashcardsapp;

import com.example.flashcardsapp.model.*;
import com.example.flashcardsapp.service.CsvImportExportService;
import com.example.flashcardsapp.util.AnimationUtils;
import com.example.flashcardsapp.util.CardDialogHelper;
import com.example.flashcardsapp.util.DialogHelper;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
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

    private final ProfileManager profileManager = new ProfileManager();
    private final FileManager fileManager = new FileManager();
    private final Preferences prefs = Preferences.userNodeForPackage(HelloController.class);

    private boolean showingDefinition = false;
    private boolean isAnimating = false;
    private boolean startFlipped = false;
    private int currentCardIndex = 0;
    private AppTheme currentTheme = AppTheme.LIGHT;

    private static final String BTNS_STYLE =
            "-fx-background-color: #ffffff; -fx-text-fill: #000000; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: transparent; " +
                    "-fx-background-radius: 10; -fx-padding: 10 20 10 20; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);";

    @FXML
    public void initialize() {
        applyCssStyles();

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setCamera(new PerspectiveCamera(false));
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
                return "-fx-padding: 10 14 10 14; -fx-background-radius: 8; " + currentTheme.getTextStyle();
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    setCursor(javafx.scene.Cursor.DEFAULT); // Обычный курсор для пустой области
                } else {
                    setText(item);
                    setFont(Font.font("System", FontWeight.BOLD, 16));
                    setStyle(getBaseStyle() + " -fx-background-color: " + (isSelected() ? currentTheme.getSelectionColor() : "transparent") + ";");
                    setCursor(javafx.scene.Cursor.HAND); // Указатель только там, где есть запись
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

    @FXML
    private void toggleSidebar() {
        rootPane.setLeft(rootPane.getLeft() != null ? null : sidebar);
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
        rootPane.getStyleClass().addAll(currentTheme == AppTheme.DARK ? List.of("dark-theme", "dark") : List.of("light-theme", "light"));

        rootPane.setStyle(currentTheme.getBgStyle());
        sidebar.setStyle(currentTheme.getSidebarStyle() + " -fx-padding: 16;");

        if (sidebarTitle != null) {
            sidebarTitle.setStyle(currentTheme.getTextStyle());
            sidebarTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
            sidebarTitle.setPadding(new Insets(12, 0, 12, 12));
        }

        if (studentMenuButton != null) {
            studentMenuButton.getStyleClass().add("menu-button");
        }

        mainContentArea.getChildren().clear();
        mainContentArea.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-viewport-background-color: transparent;");

        VBox contentBox = new VBox(20.0);
        contentBox.setStyle("-fx-padding: 24; -fx-background-color: transparent;");

        Button menuBtn = createStyledButton("☰", e -> toggleSidebar());
        Student currentStudent = profileManager.getCurrentStudent();
        currentStudentTitleLabel.setText(currentStudent != null ? "Колоды: " + currentStudent.getName() : "Выберите ученика");
        currentStudentTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        currentStudentTitleLabel.setStyle(currentTheme.getTextStyle());

        Button addDeckBtn = createStyledButton("➕ Новая колода", e -> onAddDeckClick());
        Button importCsvBtn = createStyledButton("📁 Из CSV", e -> onImportCsvClick());
        Button exportCsvBtn = createStyledButton("📤 В CSV", e -> onExportCsvClicked());

        HBox header = createHeaderBar(
                List.of(menuBtn),
                currentStudentTitleLabel,
                List.of(addDeckBtn, importCsvBtn, exportCsvBtn),
                null
        );

        deckGridPane.setStyle("-fx-background-color: transparent;");
        deckGridPane.setHgap(20);
        deckGridPane.setVgap(20);

        contentBox.getChildren().addAll(header, deckGridPane);
        scrollPane.setContent(contentBox);

        mainContentArea.getChildren().add(scrollPane);
        renderDeckGrid();
    }

    private Button createStyledButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.setStyle(BTNS_STYLE);
        AnimationUtils.applyHoverEffect(btn);
        btn.setOnAction(action);
        return btn;
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
            card.setMinWidth(170);
            card.setMinHeight(220);
            card.setStyle(currentTheme.getCardStyle() + " -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

            if (deck.getCoverImagePath() != null && !deck.getCoverImagePath().isEmpty()) {
                try {
                    ImageView coverImageView = new ImageView(new Image(deck.getCoverImagePath()));
                    coverImageView.fitWidthProperty().bind(card.prefWidthProperty());
                    coverImageView.fitHeightProperty().bind(card.prefHeightProperty());

                    // Создаем скругленную маску с радиусом 16px (соответствует карточке)
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                    clip.widthProperty().bind(card.prefWidthProperty());
                    clip.heightProperty().bind(card.prefHeightProperty());
                    clip.setArcWidth(32);  // Задаем скругление (радиус 16px * 2)
                    clip.setArcHeight(32);

                    coverImageView.setClip(clip);
                    card.getChildren().add(coverImageView);
                } catch (Exception e) {
                    System.err.println("Ошибка обложки: " + e.getMessage());
                }
            }

            VBox overlay = new VBox(8);
            overlay.setAlignment(Pos.CENTER);
            overlay.setStyle("-fx-background-color: transparent; -fx-padding: 16;");

            Label nameLabel = new Label(deck.getName());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            nameLabel.setStyle(currentTheme.getTextStyle());
            nameLabel.setWrapText(true);

            Label countLabel = new Label(deck.getCards().size() + " карточек");
            countLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            countLabel.setStyle(currentTheme.getTextStyle() + " -fx-opacity: 0.7;");

            overlay.getChildren().addAll(nameLabel, countLabel);

            MenuButton menuBtn = new MenuButton("⋮");
            menuBtn.getStyleClass().add("menu-button");

            MenuItem editItem = new MenuItem("✎ Изменить название");
            editItem.setOnAction(e -> onEditDeckClick(deck));
            MenuItem coverItem = new MenuItem("🖼 Изменить обложку");
            coverItem.setOnAction(e -> onChangeDeckCoverClick(deck));
            MenuItem copyItem = new MenuItem("📋 Скопировать ученику");
            copyItem.setOnAction(e -> onCopyDeckClick(deck));
            MenuItem deleteItem = new MenuItem("✕ Удалить");
            deleteItem.setOnAction(e -> onDeleteDeckClick(deck));

            menuBtn.getItems().addAll(editItem, coverItem, copyItem, new SeparatorMenuItem(), deleteItem);
            StackPane.setAlignment(menuBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(menuBtn, new Insets(8));

            card.getChildren().addAll(overlay, menuBtn);

            card.setOnMouseClicked(e -> {
                if (!(e.getTarget() instanceof MenuButton || (e.getTarget() instanceof Node node && node.getParent() instanceof MenuButton))) {
                    openDeckView(deck);
                }
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

    private void openDeckView(Deck deck) {
        profileManager.setCurrentDeck(deck);
        showingDefinition = startFlipped;
        currentCardIndex = 0;

        VBox studyView = new VBox(24);
        studyView.setAlignment(Pos.CENTER);
        studyView.setStyle("-fx-padding: 24; -fx-background-color: transparent;");

        Button toggleBtn = createStyledButton("☰", e -> { toggleSidebar(); rootPane.requestFocus(); });
        Button backBtn = createStyledButton("← Назад к колодам", e -> updateUI());

        Label deckTitle = new Label(deck.getName());
        deckTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        deckTitle.setStyle(currentTheme.getTextStyle());

        Button flipAllBtn = createStyledButton(startFlipped ? "🔄 Показать Термины" : "🔄 Перевернуть все", e -> {
            startFlipped = !startFlipped;
            showingDefinition = startFlipped;
            openDeckView(deck);
            rootPane.requestFocus();
        });

        Button shuffleBtn = createStyledButton("🔀 Перемешать", e -> {
            if (deck.hasCards()) {
                Collections.shuffle(deck.getCards());
                currentCardIndex = 0;
                showingDefinition = startFlipped;
                openDeckView(deck);
                rootPane.requestFocus();
            }
        });

        HBox topBar = createHeaderBar(
                List.of(toggleBtn, backBtn),
                deckTitle,
                List.of(flipAllBtn, shuffleBtn),
                () -> rootPane.requestFocus()
        );

        HBox centerBox = new HBox(40);
        centerBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        Button prevArrow = createRoundedArrowButton(false);
        Button nextArrow = createRoundedArrowButton(true);

        StackPane flashcardBox = new StackPane();
        flashcardBox.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.55));
        flashcardBox.prefHeightProperty().bind(rootPane.heightProperty().multiply(0.45));

        flashcardBox.setMinWidth(420);
        flashcardBox.setMinHeight(250);
        flashcardBox.setMaxWidth(800);
        flashcardBox.setMaxHeight(450);

        flashcardBox.setStyle(currentTheme.getCardStyle() + " -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 16, 0, 0, 4);");


        Label cardText = new Label();
        cardText.setFont(Font.font("System", FontWeight.BOLD, 45));
        cardText.setWrapText(true);
        cardText.setStyle("-fx-text-alignment: center; " + currentTheme.getTextStyle());
        flashcardBox.getChildren().add(cardText);

        VBox bottomActionContainer = new VBox(16);
        bottomActionContainer.setAlignment(Pos.CENTER);

        HBox cardActionBox = new HBox(16);
        cardActionBox.setAlignment(Pos.CENTER);

        Button addCardBtn = createStyledButton("➕ Добавить карточку", e -> {
            showCardDialog(deck, null);
            rootPane.requestFocus();
        });

        Button editCardBtn = createStyledButton("✎ Изменить эту карточку", e -> {
            if (deck.hasCards() && currentCardIndex < deck.getCards().size()) {
                showCardDialog(deck, deck.getCards().get(currentCardIndex));
            }
            rootPane.requestFocus();
        });

        Button deleteCardBtn = createStyledButton("✕ Удалить эту карточку", e -> {
            if (deck.hasCards() && currentCardIndex < deck.getCards().size()) {
                Flashcard current = deck.getCards().get(currentCardIndex);
                showConfirmationDialog("Подтверждение", "Удалить карточку \"" + current.getTerm() + "\"?", () -> {
                    if (deck.removeCard(current)) { // Используем метод вместо прямого доступа к списку
                        save();
                        openDeckView(deck);
                    }
                });
            }
            rootPane.requestFocus();
        });


        cardActionBox.getChildren().addAll(addCardBtn, editCardBtn, deleteCardBtn);

        Button restartBtn = new Button("Начать сначала");
        restartBtn.setStyle("-fx-background-color: linear-gradient(to right, #89f7fe, #66a6ff); " +
                "-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-padding: 14 36 14 36; -fx-background-radius: 30; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 2);");
        AnimationUtils.applyHoverEffect(restartBtn);
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
                cardText.setText("Колода пуста.\nНажмите '➕ Добавить карточку'");
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
            if (deck.hasCards() && currentCardIndex < deck.getCards().size()) {
                AnimationUtils.animateFlip(flashcardBox, () -> {
                    showingDefinition = !showingDefinition;
                    updateText.run();
                }, () -> isAnimating = false);
            }
        };

        flashcardBox.setOnMouseClicked(e -> {
            rootPane.requestFocus();
            flipCardAction.run();
        });

        prevArrow.setOnAction(e -> {
            rootPane.requestFocus();
            if (currentCardIndex > 0 && !isAnimating) {
                isAnimating = true;
                AnimationUtils.animateSlide(flashcardBox, false, () -> {
                    currentCardIndex--;
                    showingDefinition = startFlipped;
                    updateText.run();
                }, () -> isAnimating = false);
            }
        });

        nextArrow.setOnAction(e -> {
            rootPane.requestFocus();
            if (currentCardIndex < deck.getCards().size() && !isAnimating) {
                isAnimating = true;
                AnimationUtils.animateSlide(flashcardBox, true, () -> {
                    currentCardIndex++;
                    showingDefinition = startFlipped;
                    updateText.run();
                }, () -> isAnimating = false);
            }
        });

        centerBox.getChildren().addAll(prevArrow, flashcardBox, nextArrow);
        studyView.getChildren().addAll(topBar, centerBox, bottomActionContainer);

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

    private HBox createHeaderBar(List<Node> leftNodes, Label titleLabel, List<Node> rightNodes, Runnable settingsExtraAction) {
        HBox header = new HBox(16.0);
        header.setAlignment(Pos.CENTER_LEFT);

        if (leftNodes != null) {
            header.getChildren().addAll(leftNodes);
        }

        header.getChildren().add(titleLabel);

        // Добавляем пружину (spacer), чтобы прижать все последующие элементы вправо
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        if (rightNodes != null) {
            header.getChildren().addAll(rightNodes);
        }

        Button settingsBtn = createSettingsButton(settingsExtraAction);
        Button exitBtn = createExitButton();

        header.getChildren().addAll(settingsBtn, exitBtn);
        return header;
    }

    private Button createSettingsButton(Runnable extraAction) {
        Button settingsBtn = new Button("⚙");
        settingsBtn.getStyleClass().addAll("icon-button", "icon-button-settings");
        AnimationUtils.applyHoverEffect(settingsBtn);
        settingsBtn.setOnAction(e -> {
            onOpenSettingsClick();
            if (extraAction != null) {
                extraAction.run();
            }
        });
        return settingsBtn;
    }

    private Button createExitButton() {
        Button exitBtn = new Button("✕");
        exitBtn.getStyleClass().addAll("icon-button", "icon-button-exit");
        AnimationUtils.applyHoverEffect(exitBtn);
        exitBtn.setOnAction(e -> ((Stage) rootPane.getScene().getWindow()).close());
        return exitBtn;
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
        btn.setFocusTraversable(false);
        AnimationUtils.applyHoverEffect(btn);
        return btn;
    }

    @FXML
    private void onOpenSettingsClick() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

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

        ComboBox<AppTheme> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(AppTheme.values());
        themeComboBox.setValue(currentTheme);
        themeComboBox.setStyle("-fx-font-size: 14px;");

        Button applyBtn = createStyledButton("Применить", e -> {
            currentTheme = themeComboBox.getValue();
            prefs.put("app_theme", currentTheme.name());
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

        mainContentArea.getChildren().add(overlay);
    }

    @FXML
    protected void onAddStudentClick() {
        showTextInputDialog("Новый ученик", "Введите имя ученика:", "", name -> {
            if (profileManager.addStudent(name)) {
                save();
                updateStudentList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка!", "Ученик с таким именем уже существует!");
            }
        });
    }
    private void showConfirmationDialog(String title, String content, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType deleteButtonType = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getDialogPane().getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        DialogHelper.styleDialog(alert, currentTheme, rootPane);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                onConfirm.run();
            }
        });
    }
    @FXML
    protected void onEditStudentClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) return;

        showTextInputDialog("Редактирование ученика", "Измените имя ученика:", current.getName(), newName -> {
            if (!newName.equals(current.getName())) {
                if (profileManager.renameStudent(current.getName(), newName)) {
                    save();
                    updateStudentList();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка!", "Ученик с таким именем уже существует!");
                }
            }
        });
    }

    @FXML
    protected void onDeleteStudentClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) return;

        showConfirmationDialog("Внимание", "Удалить ученика \"" + current.getName() + "\"?", () -> {
            boolean removed = profileManager.removeStudent(current.getName());
            if (removed) {
                save();
                updateStudentList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить ученика!");
            }
        });
    }

    @FXML
    protected void onAddDeckClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка!", "Сначала выберите или создайте ученика!");
            return;
        }

        showTextInputDialog("Новая колода", "Введите название колоды:", "", name -> {
            if (profileManager.addDeckToCurrentStudent(name)) {
                save();
                updateUI();
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка!","Колода с таким названием уже существует!");
            }
        });
    }

    private void onEditDeckClick(Deck deck) {
        showTextInputDialog("Редактирование колоды", "Измените название колоды:", deck.getName(), newName -> {
            if (!newName.equals(deck.getName())) {
                if (profileManager.renameDeck(deck, newName)) {
                    save();
                    updateUI();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка!","Колода с таким названием уже существует!");
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

        ChoiceDialog<String> dialog = new ChoiceDialog<>(otherStudents.getFirst(), otherStudents);
        dialog.setTitle("Скопировать колоду");
        dialog.setHeaderText("Выберите ученика для копирования:");
        DialogHelper.styleDialog(dialog, currentTheme, rootPane);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetStudentName -> {
            Student targetStudent = profileManager.getStudents().get(targetStudentName);
            if (targetStudent != null) {
                if (targetStudent.getDecks().containsKey(deck.getName())) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка!", "Данная колода уже существует");

                } else {
                    Deck copiedDeck = deck.makeCopy(deck.getName());
                    targetStudent.addDeck(copiedDeck);
                    save();
                    showAlert(Alert.AlertType.INFORMATION, "Скопировать колоду", "Колода успешно скопирована");
                }
            }
        });
    }

    private void onDeleteDeckClick(Deck deck) {
        showConfirmationDialog("Внимание", "Удалить колоду \"" + deck.getName() + "\"?", () -> {
            boolean removed = profileManager.getCurrentStudent().removeDeck(deck.getName());
            if (removed) {
                save();
                updateUI();
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить колоду!");
            }
        });
    }

    private void showCardDialog(Deck deck, Flashcard cardToEdit) {
        CardDialogHelper.showCardDialog(cardToEdit, currentTheme, rootPane).ifPresent(pair -> {
            if (!pair.getKey().trim().isEmpty()) {
                if (cardToEdit != null) {
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

    @FXML
    private void onImportCsvClick() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка!","Сначала выберите или создайте ученика!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV-файл с карточками");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV файлы", "*.csv"));
        File file = fileChooser.showOpenDialog(mainContentArea.getScene().getWindow());

        if (file != null) {
            try {
                Deck newDeck = CsvImportExportService.importCsv(file);
                current.addDeck(newDeck);
                save();
                updateUI();
                showAlert(Alert.AlertType.INFORMATION, "Импорт в CSV", "Колода \"" + newDeck.getName() + "\" импортирована! Карточек: " + newDeck.getCards().size());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка!","Ошибка чтения CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onExportCsvClicked() {
        Student current = profileManager.getCurrentStudent();
        if (current == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка!","Сначала выберите или создайте ученика!");
            return;
        }

        Deck currentDeck = profileManager.getCurrentDeck();
        if (currentDeck == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка!", "Выберите колоду для экспорта!");
            return;
        }

        exportDeckToCsvFile(currentDeck);
    }

    private void exportDeckToCsvFile(Deck deck) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить колоду в CSV");
        fileChooser.setInitialFileName(deck.getName() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(mainContentArea.getScene().getWindow());
        if (file != null) {
            boolean success = fileManager.exportDeckToCsv(deck, file);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Колода \"" + deck.getName() + "\" успешно экспортирована!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка экспорта", "Не удалось экспортировать файл.");
            }
        }
    }

    private void showTextInputDialog(String title, String header, String defaultValue, java.util.function.Consumer<String> onSuccess) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType okButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        DialogHelper.styleDialog(dialog, currentTheme, rootPane);

        dialog.showAndWait().ifPresent(input -> {
            String trimmed = input.trim();
            if (!trimmed.isEmpty()) {
                onSuccess.accept(trimmed);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title,String content) {
        Alert alert = new Alert(type, content);
        alert.setTitle(title); // Задаем кастомный заголовок
        alert.setHeaderText(null);
        DialogHelper.styleDialog(alert, currentTheme, rootPane);
        alert.showAndWait();
    }
}