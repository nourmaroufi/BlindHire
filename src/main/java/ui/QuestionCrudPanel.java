package ui;

import Controller.questionController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import Model.choiceqcm;
import Model.question;

/**
 * QuestionCrudPanel — BorderPane root so CENTER always fills available space.
 */
public class QuestionCrudPanel extends BorderPane {

    // ── Public controls ───────────────────────────────────────────────────────
    public final Button btnGenerateQuiz        = new Button("✨ AI Generate");

    public final TableView<question>           questionTable  = new TableView<>();
    public final TableColumn<question, Number> colQId         = new TableColumn<>("ID");
    public final TableColumn<question, String> colQStatement  = new TableColumn<>("Question");
    public final TableColumn<question, String> colQPoints     = new TableColumn<>("Pts");

    public final TextArea  statementArea  = new TextArea();
    public final TextField pointsField    = new TextField();

    public final Button btnAddQuestion    = new Button("Add");
    public final Button btnUpdateQuestion = new Button("Update");
    public final Button btnDeleteQuestion = new Button("Delete");
    public final Button btnClearQuestion  = new Button("Clear");

    public final TableView<choiceqcm>            choiceTable = new TableView<>();
    public final TableColumn<choiceqcm, Number>  colCId      = new TableColumn<>("ID");
    public final TableColumn<choiceqcm, String>  colCText    = new TableColumn<>("Answer");
    public final TableColumn<choiceqcm, Boolean> colCCorrect = new TableColumn<>("✓");

    public final TextField choiceTextField = new TextField();
    public final Button btnAddChoice       = new Button("Add");
    public final Button btnUpdateChoice    = new Button("Edit");
    public final Button btnDeleteChoice    = new Button("Remove");
    public final Button btnSetCorrect      = new Button("✔ Set Correct");

    public final Label  lblStatus     = new Label("");
    public final Label  lblJobContext = new Label("");
    public final Label  lblSkillsTag = new Label("");
    public final Button btnGoTakeQuiz = new Button("Take Quiz →");
    public final Button btnLeaderboard = new Button("🏆 Leaderboard");
    public final Button btnFinish      = new Button("✓ done");

    private final questionController controller;

    public QuestionCrudPanel() { this(0, null, null); }

    public QuestionCrudPanel(int jobOfferId, String jobTitle, String requiredSkills) {
        controller = new questionController(this, jobOfferId, jobTitle, requiredSkills);
        initUI(jobOfferId > 0);
    }

    private void initUI(boolean jobLinked) {
        setStyle("-fx-background-color: #f1f5f9;");

        // TOP — header + skills strip stacked
        VBox top = new VBox(0);
        top.getChildren().add(buildHeader(jobLinked));
        if (jobLinked) top.getChildren().add(buildSkillsStrip());
        setTop(top);

        // CENTER — fills all remaining space automatically (BorderPane magic)
        HBox content = new HBox(14);
        content.setPadding(new Insets(16, 20, 16, 20));
        VBox qCard = buildQuestionsCard(jobLinked);
        VBox aCard = buildAnswersCard();
        HBox.setHgrow(qCard, Priority.ALWAYS);
        HBox.setHgrow(aCard, Priority.ALWAYS);
        content.getChildren().addAll(qCard, aCard);
        setCenter(content);

        // BOTTOM — footer
        setBottom(buildFooter(jobLinked));

        controller.init();
    }

    private HBox buildHeader(boolean jobLinked) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 14, 20));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        VBox left = new VBox(3);
        Label title = new Label(jobLinked ? "Build the Quiz" : "Quiz Builder");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 18));
        title.setTextFill(Color.web("#1a2980"));
        Label sub = new Label(jobLinked
                ? "Create screening questions for this position"
                : "Manage questions and answers for any skill");
        sub.setStyle("-fx-font-size:12; -fx-text-fill:#64748b; -fx-font-family:'Segoe UI';");
        left.getChildren().addAll(title, sub);

        if (jobLinked) {
            lblJobContext.setStyle(
                    "-fx-background-color:#f0fdfc; -fx-background-radius:999;" +
                            "-fx-border-radius:999; -fx-border-color:#26d0ce66; -fx-border-width:1;" +
                            "-fx-text-fill:#0a9494; -fx-font-size:11; -fx-font-weight:700;" +
                            "-fx-font-family:'Segoe UI'; -fx-padding:3 12;");
            lblJobContext.setVisible(false); lblJobContext.setManaged(false);
            left.getChildren().add(lblJobContext);
        }

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatus.setStyle(
                "-fx-background-color:#f0f4ff; -fx-background-radius:999;" +
                        "-fx-border-radius:999; -fx-border-color:#dde6ff; -fx-border-width:1;" +
                        "-fx-text-fill:#6e7aa8; -fx-font-size:11; -fx-font-weight:600;" +
                        "-fx-font-family:'Segoe UI'; -fx-padding:5 14;");

        styleHeaderBtn(btnGenerateQuiz, "#1a2980", "#26d0ce");
        styleHeaderBtn(btnLeaderboard, "#26d0ce", "#1a9494");

        if (jobLinked) {
            header.getChildren().addAll(left, spacer, lblStatus, btnGenerateQuiz, btnLeaderboard);
        } else {
            styleHeaderBtn(btnGoTakeQuiz, "#1a2980", "#26d0ce");
            header.getChildren().addAll(left, spacer, lblStatus, btnGenerateQuiz, btnLeaderboard, btnGoTakeQuiz);
        }
        return header;
    }

    private HBox buildSkillsStrip() {
        HBox strip = new HBox(8);
        strip.setAlignment(Pos.CENTER_LEFT);
        strip.setPadding(new Insets(7, 20, 7, 20));
        strip.setStyle("-fx-background-color:#f8faff; -fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;");
        Label icon = new Label("🔧"); icon.setStyle("-fx-font-size:12;");
        lblSkillsTag.setStyle("-fx-font-size:12; -fx-text-fill:#475569; -fx-font-family:'Segoe UI'; -fx-font-style:italic;");
        lblSkillsTag.setText("Required skills: —");
        strip.getChildren().addAll(icon, lblSkillsTag);
        return strip;
    }

    private VBox buildQuestionsCard(boolean jobLinked) {
        VBox card = makeCard();

        card.getChildren().add(cardTitle("📝  Questions", "Write each question & assign points"));



        questionTable.getColumns().addAll(colQId, colQStatement, colQPoints);
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        styleTable(questionTable);
        VBox.setVgrow(questionTable, Priority.ALWAYS); // table grows inside the card
        card.getChildren().add(questionTable);

        card.getChildren().add(new Separator());

        statementArea.setPromptText("Type your question here...");
        statementArea.setWrapText(true);
        statementArea.setPrefRowCount(3);
        statementArea.setMaxHeight(90);
        statementArea.setStyle(inputStyle());
        statementArea.setFont(Font.font("Segoe UI", 12));
        card.getChildren().add(statementArea);

        HBox pointsRow = new HBox(8); pointsRow.setAlignment(Pos.CENTER_LEFT);
        Label ptsLabel = new Label("Points:");
        ptsLabel.setStyle("-fx-font-size:12; -fx-text-fill:#64748b; -fx-font-family:'Segoe UI';");
        pointsField.setPromptText("1.00");
        pointsField.setPrefWidth(90);
        styleInput(pointsField);
        pointsRow.getChildren().addAll(ptsLabel, pointsField);
        card.getChildren().add(pointsRow);

        HBox btns = new HBox(8); btns.setAlignment(Pos.CENTER_LEFT);
        stylePrimary(btnAddQuestion); styleSecondary(btnUpdateQuestion);
        styleDanger(btnDeleteQuestion); styleGhost(btnClearQuestion);
        btns.getChildren().addAll(btnAddQuestion, btnUpdateQuestion, btnDeleteQuestion, btnClearQuestion);
        card.getChildren().add(btns);

        return card;
    }

    private VBox buildAnswersCard() {
        VBox card = makeCard();
        card.getChildren().add(cardTitle("✅  Answer Choices", "Add options & mark the correct one"));

        choiceTable.getColumns().addAll(colCId, colCText, colCCorrect);
        choiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        styleTable(choiceTable);
        VBox.setVgrow(choiceTable, Priority.ALWAYS);
        card.getChildren().add(choiceTable);

        card.getChildren().add(new Separator());

        choiceTextField.setPromptText("Answer text...");
        styleInput(choiceTextField);
        card.getChildren().add(choiceTextField);

        HBox btns = new HBox(8); btns.setAlignment(Pos.CENTER_LEFT);
        stylePrimary(btnAddChoice); styleSecondary(btnUpdateChoice);
        styleDanger(btnDeleteChoice); styleAccent(btnSetCorrect);
        btns.getChildren().addAll(btnAddChoice, btnUpdateChoice, btnDeleteChoice, btnSetCorrect);
        card.getChildren().add(btns);

        return card;
    }

    private HBox buildFooter(boolean jobLinked) {
        HBox bar = new HBox(12); bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(12, 20, 14, 20));
        bar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Label hint = new Label(jobLinked
                ? "Questions are linked to this job automatically"
                : "Select a job offer to load its questions");
        hint.setStyle("-fx-font-size:11; -fx-text-fill:#94a3b8; -fx-font-family:'Segoe UI'; -fx-font-style:italic;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(hint, spacer);

        if (jobLinked) {
            btnFinish.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a2980 0%, #26d0ce 100%);" +
                            "-fx-text-fill: white; -fx-font-weight:800; -fx-font-size:13;" +
                            "-fx-font-family:'Segoe UI'; -fx-background-radius:10;" +
                            "-fx-padding:10 28; -fx-cursor:hand;");
            bar.getChildren().add(btnFinish);
        }
        return bar;
    }

    // ── Style helpers ─────────────────────────────────────────────────────────
    private VBox makeCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius:14;" +
                "-fx-border-radius:14; -fx-border-color:#e2e8f0; -fx-border-width:1;");
        return card;
    }

    private HBox cardTitle(String title, String sub) {
        HBox row = new HBox(0); row.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        t.setTextFill(Color.web("#1a2980"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label s = new Label(sub);
        s.setStyle("-fx-font-size:11; -fx-text-fill:#94a3b8; -fx-font-family:'Segoe UI';");
        row.getChildren().addAll(t, sp, s);
        return row;
    }

    private void styleInput(Control c) {
        c.setStyle(inputStyle()); c.setPrefHeight(34);
        if (c instanceof ComboBox<?> cb) cb.setItems(FXCollections.observableArrayList());
    }

    private String inputStyle() {
        return "-fx-background-color:white; -fx-background-radius:8;" +
                "-fx-border-radius:8; -fx-border-color:#e2e8f0; -fx-border-width:1;" +
                "-fx-padding:6 10; -fx-font-family:'Segoe UI'; -fx-font-size:12;";
    }

    private void styleTable(TableView<?> t) {
        t.setStyle("-fx-background-radius:8; -fx-border-radius:8;" +
                "-fx-border-color:#e2e8f0; -fx-background-color:white;");
        t.setMinHeight(120);
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color:linear-gradient(to bottom,#1a2980,#1e3ea8);" +
                "-fx-text-fill:white; -fx-font-weight:700; -fx-font-size:12;" +
                "-fx-font-family:'Segoe UI'; -fx-background-radius:8; -fx-padding:7 16; -fx-cursor:hand;");
    }

    private void styleAccent(Button b) {
        b.setStyle("-fx-background-color:linear-gradient(to bottom,#26d0ce,#1a9a9a);" +
                "-fx-text-fill:white; -fx-font-weight:700; -fx-font-size:12;" +
                "-fx-font-family:'Segoe UI'; -fx-background-radius:8; -fx-padding:7 16; -fx-cursor:hand;");
    }

    private void styleSecondary(Button b) {
        b.setStyle("-fx-background-color:#eef2ff; -fx-text-fill:#1a2980;" +
                "-fx-font-weight:600; -fx-font-size:12; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:8; -fx-border-color:#c7d2fe; -fx-border-width:1;" +
                "-fx-border-radius:8; -fx-padding:7 16; -fx-cursor:hand;");
    }

    private void styleGhost(Button b) {
        b.setStyle("-fx-background-color:transparent; -fx-text-fill:#94a3b8;" +
                "-fx-font-family:'Segoe UI'; -fx-background-radius:8; -fx-padding:7 16; -fx-cursor:hand;");
    }

    private void styleDanger(Button b) {
        b.setStyle("-fx-background-color:#fff5f5; -fx-text-fill:#e53e3e;" +
                "-fx-font-weight:600; -fx-font-size:12; -fx-font-family:'Segoe UI';" +
                "-fx-background-radius:8; -fx-border-color:#fed7d7; -fx-border-width:1;" +
                "-fx-border-radius:8; -fx-padding:7 16; -fx-cursor:hand;");
    }

    private void styleHeaderBtn(Button b, String c1, String c2) {
        b.setStyle("-fx-background-color:linear-gradient(to bottom," + c1 + "," + c2 + ");" +
                "-fx-text-fill:white; -fx-font-weight:700; -fx-font-size:12;" +
                "-fx-font-family:'Segoe UI'; -fx-background-radius:999; -fx-padding:7 16; -fx-cursor:hand;");
    }
}