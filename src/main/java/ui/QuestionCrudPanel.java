package ui;

import controller.questionController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.choiceqcm;
import models.question;
import models.skill;

public class QuestionCrudPanel extends StackPane {

    // Public controls for controller
    public final ComboBox<skill> skillCombo = new ComboBox<>();
    public final Button btnGenerateQuiz = new Button("✨ Generate Quiz (AI)");

    public final TableView<question> questionTable = new TableView<>();
    public final TableColumn<question, Number> colQId = new TableColumn<>("ID");
    public final TableColumn<question, String> colQStatement = new TableColumn<>("Question");
    public final TableColumn<question, String> colQPoints = new TableColumn<>("Pts");

    public final TextArea statementArea = new TextArea();
    public final TextField pointsField = new TextField();

    public final Button btnAddQuestion = new Button("Add");
    public final Button btnUpdateQuestion = new Button("Update");
    public final Button btnDeleteQuestion = new Button("Delete");
    public final Button btnClearQuestion = new Button("Clear");

    public final TableView<choiceqcm> choiceTable = new TableView<>();
    public final TableColumn<choiceqcm, Number> colCId = new TableColumn<>("ID");
    public final TableColumn<choiceqcm, String> colCText = new TableColumn<>("Answer");
    public final TableColumn<choiceqcm, Boolean> colCCorrect = new TableColumn<>("✓");

    public final TextField choiceTextField = new TextField();
    public final Button btnAddChoice = new Button("Add Answer");
    public final Button btnUpdateChoice = new Button("Edit");
    public final Button btnDeleteChoice = new Button("Remove");
    public final Button btnSetCorrect = new Button("Set Correct");

    public final Label lblStatus = new Label("");
    public final Button btnGoTakeQuiz = new Button("Take Quiz →");
    public final Button btnLeaderboard = new Button("🏆 Leaderboard");

    private final questionController controller;

    public QuestionCrudPanel() {
        controller = new questionController(this);

        // Background gradient
        BackgroundFill bgFill = new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#F6FAFF")),
                        new Stop(1, Color.web("#EEF3FF"))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        );
        setBackground(new Background(bgFill));

        // Decorative bubbles
        getChildren().add(createBubblesLayer());

        // Main content card
        VBox content = new VBox(18);
        content.setPadding(new Insets(24, 28, 28, 28));
        content.setMaxWidth(1150);
        content.setFillWidth(true);

        content.getChildren().addAll(
                createTopHeader(),
                createFiltersRow(),
                createMainGrid()
        );

        StackPane.setAlignment(content, Pos.TOP_CENTER);
        getChildren().add(content);

        controller.init();
    }

    private Pane createBubblesLayer() {
        Pane p = new Pane();
        p.setPickOnBounds(false);

        Circle b1 = bubble(120, 120, 95, "#BFE3FF", 0.35);
        Circle b2 = bubble(980, 140, 130, "#C8D7FF", 0.28);
        Circle b3 = bubble(220, 640, 160, "#D8F1FF", 0.25);
        Circle b4 = bubble(1080, 680, 110, "#E1D7FF", 0.22);

        p.getChildren().addAll(b1, b2, b3, b4);
        return p;
    }

    private Circle bubble(double x, double y, double r, String color, double opacity) {
        Circle c = new Circle(r);
        c.setFill(Color.web(color, opacity));
        c.setTranslateX(x);
        c.setTranslateY(y);
        c.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.08)));
        return c;
    }

    private HBox createTopHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Quiz Builder");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        title.setTextFill(Color.web("#1D2B5A"));

        Label subtitle = new Label("Create questions + answers ");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.web("#6E7AA8"));

        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatus.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblStatus.setTextFill(Color.web("#6E7AA8"));
        lblStatus.setPadding(new Insets(6, 10, 6, 10));
        lblStatus.setStyle(pillStyle("#FFFFFF", "#DDE6FF"));

        // NEW: style + add generate button
        stylePrimaryAlt(btnGenerateQuiz);   // or stylePrimary(btnGenerateQuiz) if you want it more "blue"
        btnGenerateQuiz.setText("✨ Generate");
        btnGenerateQuiz.setPrefHeight(40);
        btnGenerateQuiz.setMinWidth(150);

        stylePrimaryAlt(btnLeaderboard);
        btnLeaderboard.setPrefHeight(40);
        btnLeaderboard.setMinWidth(150);

// add it near your other buttons (example order)


        stylePrimaryAlt(btnGoTakeQuiz); // reuse your style
        btnGoTakeQuiz.setPrefHeight(40);
        btnGoTakeQuiz.setMinWidth(140);

        // add to header (generate next to take quiz)
        header.getChildren().addAll(titles, spacer, lblStatus, btnGenerateQuiz,btnLeaderboard, btnGoTakeQuiz);

        return header;
    }

    private HBox createFiltersRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label skillLbl = new Label("Skill");
        skillLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        skillLbl.setTextFill(Color.web("#2A3A74"));

        skillCombo.setPromptText("Choose a skill...");
        skillCombo.setPrefWidth(320);
        styleInput(skillCombo);

        row.getChildren().addAll(skillLbl, skillCombo);
        return row;
    }

    private GridPane createMainGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        VBox qCard = card("Questions", "Create / update / delete questions");
        VBox aCard = card("Answers", "Add answers + mark the correct one");

        qCard.getChildren().addAll(buildQuestionSection());
        aCard.getChildren().addAll(buildAnswerSection());

        GridPane.setHgrow(qCard, Priority.ALWAYS);
        GridPane.setHgrow(aCard, Priority.ALWAYS);
        qCard.setPrefWidth(650);
        aCard.setPrefWidth(470);

        grid.add(qCard, 0, 0);
        grid.add(aCard, 1, 0);

        return grid;
    }

    private VBox buildQuestionSection() {
        VBox box = new VBox(12);

        // Table
        questionTable.getColumns().addAll(colQId, colQStatement, colQPoints);
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        questionTable.setPrefHeight(280);
        styleTable(questionTable);

        // Form
        statementArea.setPromptText("Type the question here...");
        statementArea.setWrapText(true);
        statementArea.setPrefRowCount(3);
        statementArea.setStyle(modernInputStyle());
        statementArea.setFont(Font.font("Segoe UI", 13));

        pointsField.setPromptText("Points (default 1.00)");
        styleInput(pointsField);

        HBox btns = new HBox(10);
        btns.setAlignment(Pos.CENTER_LEFT);

        stylePrimary(btnAddQuestion);
        styleSecondary(btnUpdateQuestion);
        styleDanger(btnDeleteQuestion);
        styleGhost(btnClearQuestion);

        btns.getChildren().addAll(btnAddQuestion, btnUpdateQuestion, btnDeleteQuestion, btnClearQuestion);

        box.getChildren().addAll(questionTable, new Separator(), statementArea, pointsField, btns);
        return box;
    }

    private VBox buildAnswerSection() {
        VBox box = new VBox(12);

        choiceTable.getColumns().addAll(colCId, colCText, colCCorrect);
        choiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        choiceTable.setPrefHeight(280);
        styleTable(choiceTable);

        choiceTextField.setPromptText("Answer text...");
        styleInput(choiceTextField);

        HBox btns = new HBox(10);
        btns.setAlignment(Pos.CENTER_LEFT);

        stylePrimary(btnAddChoice);
        styleSecondary(btnUpdateChoice);
        styleDanger(btnDeleteChoice);
        stylePrimaryAlt(btnSetCorrect);

        btns.getChildren().addAll(btnAddChoice, btnUpdateChoice, btnDeleteChoice, btnSetCorrect);

        box.getChildren().addAll(choiceTable, new Separator(), choiceTextField, btns);
        return box;
    }

    private VBox card(String title, String subtitle) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.86);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: rgba(200,215,255,0.8);"
        );
        card.setEffect(new DropShadow(18, Color.rgb(18, 38, 63, 0.10)));

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 16));
        t.setTextFill(Color.web("#1D2B5A"));

        Label s = new Label(subtitle);
        s.setFont(Font.font("Segoe UI", 12));
        s.setTextFill(Color.web("#6E7AA8"));

        card.getChildren().addAll(t, s);
        return card;
    }

    // ---------- Styling helpers ----------
    private void styleInput(Control c) {
        c.setStyle(modernInputStyle());
        c.setPrefHeight(38);
        if (c instanceof ComboBox<?> cb) cb.setItems(FXCollections.observableArrayList());
    }

    private String modernInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;" +
                "-fx-border-color: rgba(200,215,255,0.9);" +
                "-fx-padding: 10 12;" +
                "-fx-font-family: 'Segoe UI';";
    }

    private void styleTable(TableView<?> t) {
        t.setStyle(
                "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: rgba(200,215,255,0.9);" +
                        "-fx-background-color: rgba(255,255,255,0.90);"
        );
    }

    private void stylePrimary(Button b) {
        b.setStyle(btnStyle("#3B82F6", "#2F6FE6"));
    }

    private void stylePrimaryAlt(Button b) {
        b.setStyle(btnStyle("#60A5FA", "#4D93F0"));
    }

    private void styleSecondary(Button b) {
        b.setStyle(btnStyle("#EEF2FF", "#E0E7FF") +
                "-fx-text-fill: #1D2B5A;" +
                "-fx-border-color: rgba(180,200,255,0.9);" +
                "-fx-border-radius: 999;" +
                "-fx-border-width: 1;");
    }

    private void styleGhost(Button b) {
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #5563A8;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 10 16;" +
                        "-fx-cursor: hand;"
        );
    }

    private void styleDanger(Button b) {
        b.setStyle(btnStyle("#FCA5A5", "#F87171") + "-fx-text-fill: #5B1B1B;");
    }

    private String btnStyle(String c1, String c2) {
        return "-fx-background-color: linear-gradient(to bottom, " + c1 + ", " + c2 + ");" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: 700;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 10 16;" +
                "-fx-cursor: hand;";
    }

    private String pillStyle(String bg, String border) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-width: 1;";
    }
}
