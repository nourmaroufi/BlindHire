package ui;

import controller.QuizPassController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.question;
import models.skill;

public class TakeQuizPanel extends StackPane {

    public final ComboBox<LangItem> langCombo = new ComboBox<>();
    public final Button btnBack = new Button("← Back");
    public final ComboBox<skill> skillCombo = new ComboBox<>();
    public final Label lblTimer = new Label("01:00");
    public final TextField userIdField = new TextField();
    public final Button btnLoad = new Button("Load Quiz");
    public final Button btnSubmit = new Button("Submit & Save Score");

    public final VBox questionsBox = new VBox(12);
    public final ScrollPane scroll = new ScrollPane(questionsBox);

    public final Label lblStatus = new Label("Ready");

    private final QuizPassController controller;

    public TakeQuizPanel() {
        controller = new QuizPassController(this);

        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#F6FAFF")),
                        new Stop(1, Color.web("#EEF3FF"))),
                CornerRadii.EMPTY, Insets.EMPTY
        )));

        getChildren().add(bubbles());

        VBox root = new VBox(16);
        root.setPadding(new Insets(24, 28, 28, 28));
        root.setMaxWidth(1150);

        root.getChildren().addAll(header(), topRow(), quizArea());
        StackPane.setAlignment(root, Pos.TOP_CENTER);
        getChildren().add(root);

        controller.init();
    }

    private Pane bubbles() {
        Pane p = new Pane();
        p.setPickOnBounds(false);
        p.getChildren().addAll(
                bubble(120, 120, 95, "#BFE3FF", 0.35),
                bubble(980, 140, 130, "#C8D7FF", 0.28),
                bubble(220, 640, 160, "#D8F1FF", 0.25),
                bubble(1080, 680, 110, "#E1D7FF", 0.22)
        );
        return p;
    }

    private Circle bubble(double x, double y, double r, String c, double op) {
        Circle circle = new Circle(r);
        circle.setFill(Color.web(c, op));
        circle.setTranslateX(x);
        circle.setTranslateY(y);
        circle.setEffect(new DropShadow(18, Color.rgb(0,0,0,0.08)));
        return circle;
    }

    private HBox header() {
        HBox h = new HBox(12);
        h.setAlignment(Pos.CENTER_LEFT);

        btnBack.setStyle(btnGhost());

        VBox titles = new VBox(4);
        Label title = new Label("Take Quiz");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        title.setTextFill(Color.web("#1D2B5A"));

        Label sub = new Label("Choose a skill → answer QCM → get score saved for user");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web("#6E7AA8"));
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatus.setStyle(pillStyle());
        lblStatus.setTextFill(Color.web("#6E7AA8"));
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblTimer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: #DDE6FF;" +
                        "-fx-padding: 6 12;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: #1D2B5A;"
        );

        h.getChildren().addAll(btnBack, titles, spacer,lblTimer, lblStatus);
        return h;
    }

    private HBox topRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label u = new Label("User ID");
        u.setTextFill(Color.web("#2A3A74"));
        u.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        userIdField.setPromptText("e.g. 12");
        userIdField.setPrefWidth(140);
        userIdField.setStyle(inputStyle());

        Label s = new Label("Skill");
        s.setTextFill(Color.web("#2A3A74"));
        s.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        skillCombo.setPromptText("Select skill...");
        skillCombo.setPrefWidth(320);
        skillCombo.setStyle(inputStyle());

        btnLoad.setStyle(btnPrimary());
        btnSubmit.setStyle(btnPrimaryAlt());

        Label l = new Label("Language");
        l.setTextFill(Color.web("#2A3A74"));
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));

        langCombo.setPromptText("Choose...");
        langCombo.setPrefWidth(200);
        langCombo.setStyle(inputStyle());

        row.getChildren().addAll(l, langCombo);

        row.getChildren().addAll(u, userIdField, s, skillCombo, btnLoad, btnSubmit);
        return row;
    }

    private VBox quizArea() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.86);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: rgba(200,215,255,0.8);"
        );
        card.setEffect(new DropShadow(18, Color.rgb(18, 38, 63, 0.10)));

        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        questionsBox.setPadding(new Insets(8));

        card.getChildren().addAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return card;
    }
    public static class LangItem {
        private final String code;
        private final String label;

        public LangItem(String code, String label) {
            this.code = code;
            this.label = label;
        }
        public String getCode() { return code; }
        @Override public String toString() { return label; } // what the ComboBox shows
    }
    // Styling strings
    private String inputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;" +
                "-fx-border-color: rgba(200,215,255,0.9);" +
                "-fx-padding: 10 12;" +
                "-fx-font-family: 'Segoe UI';";
    }
    private String btnPrimary() {
        return "-fx-background-color: linear-gradient(to bottom, #3B82F6, #2F6FE6);" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: 800;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 10 16;" +
                "-fx-cursor: hand;";
    }
    private String btnPrimaryAlt() {
        return "-fx-background-color: linear-gradient(to bottom, #60A5FA, #4D93F0);" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: 800;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 10 16;" +
                "-fx-cursor: hand;";
    }
    private String btnGhost() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #5563A8;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: 700;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 10 16;" +
                "-fx-cursor: hand;";
    }
    private String pillStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: #DDE6FF;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 6 10;";
    }
}
