package com.teagang.projecttea;

import atlantafx.base.controls.Notification;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;

import javafx.animation.Animation;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class MainApp extends Application {

    public static final int MAX_SIZE = 5;

    private final StackPane root = new StackPane();
    private final VBox mainPane = new VBox();
    private final HBox controlContainer = new HBox();

    private final FontIcon arrowIcon = new FontIcon(Material2AL.ARROW_FORWARD);
    private final VBox inputBox = new VBox();
    private final VBox resultsBox = new VBox();
    private final GridPane resultPane = new GridPane();
    private final Label inputLabel = new Label();
    private final GridPane inputMatrix = new GridPane();
    private final GridPane resultMatrix = new GridPane();
    private Notification prevNotification = null;

    private final HBox navbar = new HBox();
    private final HBox contentHBox = new HBox();
    private final HBox botSection = new HBox();
    private final Separator sep1 = new Separator(Orientation.HORIZONTAL);
    private final Separator sep2 = new Separator(Orientation.HORIZONTAL);

    private final TextField[][] inputEntries = new TextField[MAX_SIZE][MAX_SIZE];
    private final TextField[][] resultFields = new TextField[MAX_SIZE][MAX_SIZE];

    private final Random random = new Random();
    private boolean inResultsView = false;
    private int matrixSize = 5;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private void showAlert() {
        Alert missingValuesAlert = new Alert(Alert.AlertType.ERROR);
        missingValuesAlert.setTitle("Input Matrix is Singular");
        missingValuesAlert.setHeaderText(null);
        missingValuesAlert.setContentText("Inverse matrix cannot be calculate because the provided input matrix is singular. Please revise.");
        missingValuesAlert.initOwner(mainPane.getScene().getWindow());
        missingValuesAlert.show();
    }

    private void attemptSolve(String[][] data) throws SingularMatrixException {
        Solver solver = new Solver();
        String[][] resultData = solver.solveInverse(data);

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                resultFields[i][j].setText(resultData[i][j]);
            }
        }
    }

    private boolean process() {
        String[][] data = new String[matrixSize][matrixSize];

        try {
            checkEmptyFields(data);
        } catch (Exception e) {
            return true;
        }

        try {
            resizeMatrix(matrixSize, resultMatrix, resultFields);
            attemptSolve(data);
        } catch (SingularMatrixException e) {
            showAlert();
            return true;
        }
        return false;
    }

    private void handleCalcBtn() {
        if (process()) return;

        if (inResultsView) {
            resizeMatrix(matrixSize, resultMatrix, resultFields);

            Animation anim = Animations.flash(resultMatrix);
            anim.playFromStart();
        } else {
            updateUI();
            inResultsView = true;
        }
    }

    private void handleRandomBtn() {
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                // Generate a random integer between -99 and 99
                int value = random.nextInt(-99, 101);
                inputEntries[i][j].setText(Integer.toString(value));
            }
        }

        if (!inResultsView) {
            return;
        }

        if (process()) return;

        if (inResultsView) {
            Animation anim = Animations.flash(resultMatrix);
            anim.playFromStart();
        }
    }

    private void updateUIOnClear() {
        arrowIcon.setVisible(false);
        arrowIcon.setManaged(false);

        Animation anim1 = Animations.slideOutLeft(inputBox, Duration.millis(500));
        anim1.playFromStart();

        resultsBox.setManaged(false);

        Animation anim5 = Animations.slideOutRight(resultsBox, Duration.millis(500));
        anim5.setOnFinished(actionEvent -> {
            changeGridConstraints(100, 0, 0);
            resultsBox.setVisible(false);
            inputLabel.setVisible(false);
            inputLabel.setManaged(false);
            inputBox.setVisible(false);

            executor.schedule(() -> {
                Animation animInner = Animations.zoomIn(inputBox, Duration.millis(400));
                animInner.playFromStart();
                inputBox.setVisible(true);
            }, 100, TimeUnit.MILLISECONDS);
        });
        anim5.playFromStart();
    }

    private void handleClearBtn() {
        for (TextField[] fieldList : inputEntries) {
            for (TextField field : fieldList) {
                field.setText("");
            }
        }

        if (!inResultsView) {
            return;
        }

        updateUIOnClear();
        inResultsView = false;
    }

    private void initNavBar() {
        ToggleSwitch darkToggle = new ToggleSwitch();
        darkToggle.setText("Dark Mode");
        darkToggle.setSelected(false);
        darkToggle.setFocusTraversable(false);
        darkToggle.selectedProperty().addListener((obs, old, val) -> {
            String toAdd, toRemove;

            if (val) {
                javafx.application.Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
                toAdd = "dark";
                toRemove = "light";

                contentHBox.getStyleClass().remove("light");
                sep1.getStyleClass().remove("sep-light");
                sep2.getStyleClass().remove("sep-light");
            } else {
                javafx.application.Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                toAdd = "light";
                toRemove = "dark";

                contentHBox.getStyleClass().add("light");
                sep1.getStyleClass().add("sep-light");
                sep2.getStyleClass().add("sep-light");
            }

            navbar.getStyleClass().remove(toRemove);
            botSection.getStyleClass().remove(toRemove);

            navbar.getStyleClass().add(toAdd);
            botSection.getStyleClass().add(toAdd);
        });

        navbar.setAlignment(Pos.CENTER_RIGHT);
        navbar.setPadding(new Insets(10, 10, 10, 0));
        navbar.getStyleClass().add("light");
        navbar.getChildren().add(darkToggle);

        mainPane.getChildren().add(navbar);
    }

    private boolean isValidEntry(String text) {
        if (text.matches("-?(0|[1-9]\\d*)/([1-9]\\d*)*") || text.matches("[-.]") || text.isEmpty()) {
            return true;
        }

        if (text.contains("/") || text.length() != text.trim().length()) {
            return false;
        }

        try {
            Double.parseDouble(text);

            return !text.matches("-?00+.?\\d*");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showInvalidInputPopup(GridPane pane) {
        Animation anim = Animations.shakeX(pane);
        anim.playFromStart();

        if (root.getChildren().contains(prevNotification)) {
            Animation replayAnim = Animations.slideInDown(prevNotification, Duration.millis(250));
            replayAnim.playFromStart();
            return;
        }

        Notification notification = new Notification(
                "You are entering an invalid input.",
                new FontIcon(Material2OutlinedAL.HELP_OUTLINE)
        );
        notification.getStyleClass().addAll(Styles.ELEVATED_1, Styles.DANGER);
        notification.setPrefHeight(Region.USE_PREF_SIZE);
        notification.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane.setAlignment(notification, Pos.TOP_RIGHT);
        StackPane.setMargin(notification, new Insets(10, 10, 0, 0));

        Animation openAnim = Animations.slideInDown(notification, Duration.millis(250));
        if (!root.getChildren().contains(notification)) {
            root.getChildren().add(notification);
        }
        openAnim.playFromStart();

        notification.setOnClose(event -> {
            Animation closeAnim = Animations.slideOutUp(notification, Duration.millis(250));
            closeAnim.setOnFinished(actionEvent -> root.getChildren().remove(notification));
            closeAnim.playFromStart();
        });
        prevNotification = notification;
    }

    private void populateWithTextFields(GridPane pane) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (isValidEntry(newText)) {
                return change;
            }

            showInvalidInputPopup(pane);

            return null;
        };

        for (int i = 0; i < MAX_SIZE; i++) {
            for (int j = 0; j < MAX_SIZE; j++) {
                TextFormatter<String> textFormatter = new TextFormatter<>(filter);
                inputEntries[i][j] = new TextField();
                inputEntries[i][j].setPrefHeight(200);
                inputEntries[i][j].setAlignment(Pos.CENTER);
                inputEntries[i][j].setPadding(new Insets(0, 10, 0, 10));
                inputEntries[i][j].setTextFormatter(textFormatter);

                final int fi = i, fj = j;

                inputEntries[i][j].focusedProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue) {
                        try {
                            Double.parseDouble(inputEntries[fi][fj].getText());
                        } catch (NumberFormatException e) {
                            inputEntries[fi][fj].setText("");
                        }
                    }
                });

                inputEntries[i][j].setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ESCAPE && root.getChildren().contains(prevNotification)) {
                        Animation anim = Animations.slideOutUp(prevNotification, Duration.millis(250));
                        anim.setOnFinished(actionEvent -> root.getChildren().remove(prevNotification));
                        anim.playFromStart();
                    }

                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        handleCalcBtn();
                    }
                });

                pane.add(inputEntries[i][j], j, i);
            }
        }
    }

    private void genMatrixBorder(GridPane pane) {
        for (int i = 0; i < MAX_SIZE; i++) {
            for (int j = 0; j < MAX_SIZE; j++) {
                resultFields[i][j] = new TextField();
                resultFields[i][j].setPrefHeight(200);
                resultFields[i][j].setEditable(false);
                resultFields[i][j].setFocusTraversable(false);
                resultFields[i][j].setAlignment(Pos.CENTER);
                resultFields[i][j].setCursor(Cursor.DEFAULT);
                resultFields[i][j].setPadding(new Insets(0, 10, 0, 10));
                pane.add(resultFields[i][j], j, i);
            }
        }
    }

    private void initInputArea(GridPane pane) {
        inputLabel.setText("Input Matrix");
        inputLabel.getStyleClass().addAll(Styles.TEXT_CAPTION, Styles.TEXT_BOLD);
        inputLabel.setVisible(false);
        inputLabel.setManaged(false);

        inputMatrix.setMaxWidth(275);
        inputMatrix.setMaxHeight(275);
        inputMatrix.setMinWidth(275);
        inputMatrix.setMinHeight(275);
        populateWithTextFields(inputMatrix);

        inputBox.setSpacing(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getChildren().addAll(inputLabel, inputMatrix);

        pane.add(inputBox, 0, 0);
    }

    private void initResultsArea(GridPane pane) {
        Label label = new Label("Inverse Matrix");
        label.getStyleClass().add(Styles.TEXT_CAPTION);

        resultMatrix.setMaxWidth(275);
        resultMatrix.setMaxHeight(275);
        resultMatrix.setMinWidth(275);
        resultMatrix.setMinHeight(275);
        genMatrixBorder(resultMatrix);

        resultsBox.setSpacing(10);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.getChildren().addAll(label, resultMatrix);
        resultsBox.setVisible(false);

        pane.add(resultsBox, 2, 0);
    }

    private void initMatrixAreas(Pane pane) {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(0);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(0);
        resultPane.getColumnConstraints().addAll(col1, col2, col3);

        resultPane.setAlignment(Pos.CENTER);
        VBox.setVgrow(resultPane, Priority.ALWAYS);

        initInputArea(resultPane);

        HBox arrowBox = new HBox();
        arrowBox.setAlignment(Pos.CENTER);
        arrowBox.getChildren().add(arrowIcon);
        arrowIcon.setVisible(false);
        resultPane.add(arrowBox, 1, 0);

        initResultsArea(resultPane);
        pane.getChildren().add(resultPane);
    }

    private void changeGridConstraints(double c1, double c2, double c3) {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(c1);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(c2);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(c3);

        resultPane.getColumnConstraints().clear();
        resultPane.getColumnConstraints().addAll(col1, col2, col3);
    }

    private void updateUI() {
        changeGridConstraints(48, 4, 48);

        inputLabel.setVisible(true);
        inputLabel.setManaged(true);
        arrowIcon.setVisible(true);
        resultsBox.setVisible(true);
        resultsBox.setManaged(true);

        Animation anim1 = Animations.slideInLeft(inputBox, Duration.seconds(1));
        Animation anim2 = Animations.slideInUp(arrowIcon, Duration.seconds(1));
        Animation anim3 = Animations.slideInRight(resultsBox, Duration.seconds(1));

        anim1.playFromStart();
        anim2.playFromStart();
        anim3.playFromStart();
    }

    private void resizeMatrix(int matrixSize, GridPane pane, TextField[][] fields) {
        double[] percentMapping = {50, 1.0 / 3 * 100, 25, 20};
        pane.getColumnConstraints().clear();

        for (int i = 0; i < MAX_SIZE; i++) {
            for (int j = 0; j < MAX_SIZE; j++) {
                if (GridPane.getRowIndex(fields[i][j]) >= matrixSize || GridPane.getColumnIndex(fields[i][j]) >= matrixSize) {
                    fields[i][j].setVisible(false);
                    fields[i][j].setManaged(false);
                    continue;
                }

                fields[i][j].setVisible(true);
                fields[i][j].setManaged(true);
            }
        }

        for (int i = 0; i < MAX_SIZE; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(i < matrixSize ? percentMapping[matrixSize - 2] : 0);
            pane.getColumnConstraints().add(colConstraints);
        }
    }

    private void checkEmptyFields(String[][] data) throws Exception {
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (inputEntries[i][j].getText().isBlank()) {
                    Alert missingValuesAlert = new Alert(Alert.AlertType.ERROR);
                    missingValuesAlert.setTitle("Missing Input Values");
                    missingValuesAlert.setHeaderText(null);
                    missingValuesAlert.setContentText("There are null or blank entries in the input matrix. Please enter missing values.");
                    missingValuesAlert.initOwner(mainPane.getScene().getWindow());
                    missingValuesAlert.show();
                    throw new Exception();
                }

                data[i][j] = inputEntries[i][j].getText();
            }
        }
    }

    private void initButtonsArea(Pane pane) {
        Button clearButton = new Button("Clear", new FontIcon(Material2AL.CLEAR_ALL));
        clearButton.getStyleClass().addAll(Styles.DANGER,Styles.ROUNDED);
        clearButton.setOnMouseClicked(mouseEvent -> handleClearBtn());
        clearButton.setFocusTraversable(false);
        clearButton.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                handleClearBtn();
            }
        });

        Button randomBtn = new Button("Randomize", new FontIcon(Material2MZ.SWAP_CALLS));
        randomBtn.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
        randomBtn.setOnMouseClicked(mouseEvent -> handleRandomBtn());
        randomBtn.setFocusTraversable(false);
        randomBtn.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                handleRandomBtn();
            }
        });

        Button calcButton = new Button("Calculate", new FontIcon(Material2AL.CALCULATE));
        calcButton.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED);
        calcButton.setDefaultButton(true);
        calcButton.setOnMouseClicked(mouseEvent -> handleCalcBtn());
        calcButton.setFocusTraversable(false);
        calcButton.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                handleCalcBtn();
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 0, 10, 0));
        buttonBox.setSpacing(10);

        buttonBox.getChildren().addAll(clearButton, randomBtn, calcButton);
        pane.getChildren().add(buttonBox);
    }

    private void initContentArea() {
        VBox solveArea = new VBox();
        HBox.setHgrow(solveArea, Priority.ALWAYS);

        initControlUI(solveArea);
        initMatrixAreas(solveArea);
        initButtonsArea(solveArea);

        contentHBox.getStyleClass().add("light");
        VBox.setVgrow(contentHBox, Priority.ALWAYS);

        contentHBox.getChildren().add(solveArea);
        mainPane.getChildren().add(contentHBox);
    }

    private void resizeInputMatrix(int dimension) {
        resizeMatrix(dimension, inputMatrix, inputEntries);
        matrixSize = dimension;
    }

    private void initControlUI(Pane pane) {
        Label controlLabel = new Label("Dimension:");
        controlLabel.setAlignment(Pos.CENTER);
        controlLabel.getStyleClass().add(Styles.TEXT_CAPTION);

        Slider sizeSlider = new Slider(2, 5, 5);
        sizeSlider.setMajorTickUnit(1);
        sizeSlider.setMinorTickCount(0);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setSnapToTicks(true);
        sizeSlider.setFocusTraversable(false);
        sizeSlider.valueProperty().addListener((observableValue, old, value) -> resizeInputMatrix(value.intValue()));

        controlContainer.setAlignment(Pos.TOP_CENTER);
        controlContainer.setSpacing(10);
        controlContainer.setPadding(new Insets(10, 0, 0, 0));
        controlContainer.getChildren().addAll(controlLabel, sizeSlider);

        pane.getChildren().add(controlContainer);
        mainPane.getChildren().add(pane);
    }

    private void initBotSection() {
        FontIcon copyrightIcon = new FontIcon(Material2AL.COPYRIGHT);
        Label copyrightLabel = new Label("Developed by TEA-Gang");
        copyrightLabel.getStyleClass().add(Styles.TEXT_SMALL);

        botSection.setAlignment(Pos.CENTER_RIGHT);
        botSection.setSpacing(6);
        botSection.setPadding(new Insets(10));
        botSection.getStyleClass().add("light");
        botSection.getChildren().addAll(copyrightIcon, copyrightLabel);

        mainPane.getChildren().add(botSection);
    }

    private void populateMainPane() {
        sep1.setPadding(Insets.EMPTY);
        sep1.getStyleClass().add("sep-light");
        sep2.setPadding(Insets.EMPTY);
        sep2.getStyleClass().add("sep-light");

        initNavBar();
        mainPane.getChildren().add(sep1);
        initContentArea();
        mainPane.getChildren().add(sep2);
        initBotSection();
    }

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        populateMainPane();

        FontIcon logo = new FontIcon(Material2AL.LOCAL_FIRE_DEPARTMENT);
        StackPane.setAlignment(logo, Pos.TOP_LEFT);
        StackPane.setMargin(logo, new Insets(15, 0, 0, 10));

        Label title = new Label("Inverse Matrix Calculator");
        title.getStyleClass().add(Styles.TITLE_3);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(8, 0, 0, 0));

        root.getChildren().addAll(mainPane, logo, title);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/app.css")).toExternalForm());
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE && root.getChildren().contains(prevNotification)) {
                Animation anim = Animations.slideOutUp(prevNotification, Duration.millis(250));
                anim.setOnFinished(actionEvent -> root.getChildren().remove(prevNotification));
                anim.playFromStart();
            }
        });

        stage.setTitle("Inverse Matrix Calculator");
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setOnCloseRequest(windowEvent -> executor.shutdown());
        stage.show();

        inputEntries[0][0].requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}