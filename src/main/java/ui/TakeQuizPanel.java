package ui;

import Controller.QuizPassController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TakeQuizPanel extends StackPane {

    // ── Controls accessed by QuizPassController ───────────────────────────────
    public final ComboBox<LangItem> langCombo        = new ComboBox<>();
    public final Button             btnBack           = new Button("← Back");
    public final TextField          jobOfferIdField   = new TextField();
    public final Label              lblJobTitle       = new Label("");
    public final Label              lblRequiredSkills = new Label("");
    public final Label              lblTimer          = new Label("01:00");
    public final TextField          userIdField       = new TextField();
    public final Button             btnLoad           = new Button("Load Quiz");
    public final Button             btnSubmit         = new Button("Submit & Save Score");
    public final VBox               questionsBox      = new VBox(14);
    public final ScrollPane         scroll            = new ScrollPane(questionsBox);
    public final Label              lblStatus         = new Label("Ready");
    public final HBox               inputRow          = new HBox(12);

    public final QuizPassController controller;

    public TakeQuizPanel() {
        controller = new QuizPassController(this);

        // Clip to own bounds so decorations never bleed over the sidebar.
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#F0F6FF")),
                        new Stop(1, Color.web("#E8F0FF"))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().add(buildDecoLayer());

        VBox root = new VBox(0);
        root.setMaxWidth(Double.MAX_VALUE);
        VBox body = buildBody();
        VBox.setVgrow(body, Priority.ALWAYS);
        root.getChildren().addAll(buildTopBar(), body);
        VBox.setVgrow(root, Priority.ALWAYS);
        getChildren().add(root);

        controller.init();
    }

    // ── Decorative background layer ───────────────────────────────────────────
    private Pane buildDecoLayer() {
        Pane p = new Pane(); p.setPickOnBounds(false); p.setMouseTransparent(true);
        for (int x = 0; x < 1600; x += 80) {
            Rectangle r = new Rectangle(x, 0, 1, 1200);
            r.setFill(Color.web("#2563EB", 0.025)); p.getChildren().add(r);
        }
        for (int y = 0; y < 1200; y += 80) {
            Rectangle r = new Rectangle(0, y, 1600, 1);
            r.setFill(Color.web("#2563EB", 0.025)); p.getChildren().add(r);
        }
        p.getChildren().addAll(
                blob(80,   60,  180, "#3B82F6", 0.07),
                blob(1150, 90,  220, "#6366F1", 0.055),
                blob(280,  580, 160, "#60A5FA", 0.05),
                blob(1020, 620, 150, "#818CF8", 0.045)
        );
        return p;
    }

    private Circle blob(double x, double y, double r, String c, double op) {
        Circle circle = new Circle(r);
        circle.setFill(Color.web(c, op));
        circle.setTranslateX(x); circle.setTranslateY(y);
        circle.setEffect(new GaussianBlur(55));
        return circle;
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 28, 14, 28));
        bar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.88);" +
                        "-fx-border-color: rgba(37,99,235,0.1); -fx-border-width: 0 0 1 0;");
        bar.setEffect(new DropShadow(8, Color.rgb(30, 58, 138, 0.06)));

        btnBack.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #3B82F6;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: 700; -fx-font-size: 13;" +
                        "-fx-cursor: hand; -fx-padding: 6 14; -fx-background-radius: 999;" +
                        "-fx-border-color: rgba(59,130,246,0.3); -fx-border-width: 1; -fx-border-radius: 999;");

        VBox titles = new VBox(2);
        Label titleLbl = new Label("Take Quiz");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        titleLbl.setTextFill(Color.web("#1e3a8a"));

        lblJobTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblJobTitle.setTextFill(Color.web("#3B82F6"));
        lblJobTitle.setVisible(false); lblJobTitle.setManaged(false);

        lblRequiredSkills.setFont(Font.font("Segoe UI", 11));
        lblRequiredSkills.setTextFill(Color.web("#64748B"));
        lblRequiredSkills.setStyle("-fx-font-style: italic;");
        lblRequiredSkills.setVisible(false); lblRequiredSkills.setManaged(false);

        titles.getChildren().addAll(titleLbl, lblJobTitle, lblRequiredSkills);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatus.setStyle(
                "-fx-background-color: #EEF2FF; -fx-background-radius: 999;" +
                        "-fx-border-radius: 999; -fx-border-color: #C7D2FE; -fx-border-width: 1;" +
                        "-fx-text-fill: #4F46E5; -fx-font-size: 11; -fx-font-weight: 700;" +
                        "-fx-font-family: 'Segoe UI'; -fx-padding: 5 14;");

        lblTimer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 999;" +
                        "-fx-border-radius: 999; -fx-border-color: #BFDBFE; -fx-border-width: 2;" +
                        "-fx-padding: 7 16; -fx-font-family: 'Segoe UI'; -fx-font-weight: 800;" +
                        "-fx-font-size: 15; -fx-text-fill: #1e3a8a;");
        lblTimer.setEffect(new DropShadow(6, Color.web("#3B82F6", 0.15)));

        bar.getChildren().addAll(btnBack, titles, spacer, lblStatus, lblTimer);
        return bar;
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    private VBox buildBody() {
        VBox body = new VBox(16);
        body.setPadding(new Insets(20, 28, 24, 28));
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox quizCard = buildQuizCard();
        VBox.setVgrow(quizCard, Priority.ALWAYS);
        body.getChildren().addAll(buildInputRow(), quizCard);
        return body;
    }

    // ── Input row ─────────────────────────────────────────────────────────────
    private HBox buildInputRow() {
        HBox row = inputRow;
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 16; -fx-border-radius: 16;" +
                        "-fx-border-color: rgba(59,130,246,0.15); -fx-border-width: 1;");
        row.setEffect(new DropShadow(6, Color.web("#3B82F6", 0.06)));

        row.getChildren().addAll(
                inputGroup("User ID",      userIdField,     "e.g. 5", 100),
                inputGroup("Job Offer ID", jobOfferIdField, "e.g. 3", 100)
        );

        // Language group
        HBox langGroup = new HBox(6); langGroup.setAlignment(Pos.CENTER_LEFT);
        Label langLbl = new Label("Language");
        langLbl.setStyle("-fx-font-family:'Segoe UI';-fx-font-weight:700;-fx-font-size:12;-fx-text-fill:#374151;");
        langCombo.setPromptText("Language...");
        langCombo.setPrefWidth(170);
        langCombo.setStyle(inputStyle());
        langGroup.getChildren().addAll(langLbl, langCombo);
        row.getChildren().add(langGroup);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().add(sp);

        btnLoad.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3B82F6, #2563EB);" +
                        "-fx-text-fill: white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                        "-fx-background-radius:10; -fx-padding:10 22; -fx-cursor:hand; -fx-font-size:13;");
        btnLoad.setEffect(new DropShadow(8, Color.web("#2563EB", 0.3)));
        row.getChildren().add(btnLoad);

        return row;
    }

    private HBox inputGroup(String labelTxt, TextField field, String prompt, double width) {
        HBox g = new HBox(6); g.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(labelTxt);
        lbl.setStyle("-fx-font-family:'Segoe UI';-fx-font-weight:700;-fx-font-size:12;-fx-text-fill:#374151;");
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(inputStyle());
        g.getChildren().addAll(lbl, field);
        return g;
    }

    // ── Quiz card ─────────────────────────────────────────────────────────────
    private VBox buildQuizCard() {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.90);" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-border-color: rgba(59,130,246,0.14); -fx-border-width: 1;");
        card.setEffect(new DropShadow(18, Color.web("#1e3a8a", 0.08)));
        VBox.setVgrow(card, Priority.ALWAYS);

        // Card header strip
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(13, 20, 13, 20));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #EFF6FF, #F5F3FF);" +
                        "-fx-background-radius: 20 20 0 0;" +
                        "-fx-border-color: rgba(59,130,246,0.1); -fx-border-width: 0 0 1 0;");
        Label hLbl = new Label("📝  Questions");
        hLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        hLbl.setTextFill(Color.web("#1e3a8a"));
        header.getChildren().add(hLbl);

        // Scroll area
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent;" +
                        "-fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        questionsBox.setPadding(new Insets(18, 22, 18, 22));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Submit button as card footer
        btnSubmit.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563EB, #4F46E5);" +
                        "-fx-text-fill: white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                        "-fx-font-size: 14; -fx-background-radius: 0 0 20 20; -fx-padding: 16 0;" +
                        "-fx-cursor: hand;");
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setEffect(new DropShadow(12, Color.web("#2563EB", 0.25)));

        card.getChildren().addAll(header, scroll, btnSubmit);
        return card;
    }

    private String inputStyle() {
        return "-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-border-radius: 10; -fx-border-color: #DBEAFE; -fx-border-width: 1.5;" +
                "-fx-padding: 8 12; -fx-font-family:'Segoe UI'; -fx-font-size: 12;";
    }

    // ── LangItem ──────────────────────────────────────────────────────────────
    public static class LangItem {
        private final String code;
        private final String label;
        public LangItem(String code, String label) { this.code = code; this.label = label; }
        public String getCode() { return code; }
        @Override public String toString() { return label; }
    }
}