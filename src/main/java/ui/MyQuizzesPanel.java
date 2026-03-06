package ui;

import Controller.MyQuizzesController;
import Controller.MyQuizzesController.QuizRow;
import Model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

/**
 * MyQuizzesPanel
 *
 * Displays every job application as a quiz-status row.
 * Each row shows:
 *   • Job offer name
 *   • Candidature status   (pending / accepted / rejected)
 *   • Quiz status          (Not Taken / Passed / Failed)
 *   • Score %              (only if taken)
 *   • Result badge         (🏆 Passed / ❌ Failed — only if taken)
 *   • Action button        (▶ Take Quiz — only if accepted AND not yet taken)
 *
 * Called from HomePage.openQuizzesPage():
 *   new MyQuizzesPanel(currentUser,
 *                      () -> root.setCenter(createMainContent()),
 *                      jobOfferId -> openQuizPanel(jobOfferId))
 */
public class MyQuizzesPanel extends StackPane {

    // ── Colour palette  (identical to TakeQuizPanel) ─────────────────────────
    private static final String C_BG_A  = "#e8f5f3";
    private static final String C_BG_B  = "#e2eef8";
    private static final String C_NAVY  = "#0c4a6e";
    private static final String C_BLUE  = "#0FAFDD";
    private static final String C_INDIG = "#0FAFDD";
    private static final String C_GREEN = "#10B981";
    private static final String C_AMBER = "#F59E0B";
    private static final String C_ROSE  = "#EF4444";
    private static final String C_SLATE = "#64748B";
    private static final String C_CARD  = "rgba(255,255,255,0.95)";

    // ── State ─────────────────────────────────────────────────────────────────
    private final User                currentUser;
    private final Runnable            onBack;
    private final Consumer<Integer>   onTakeQuiz;
    private final MyQuizzesController controller = new MyQuizzesController();

    private List<QuizRow> allRows;
    private VBox          listBox;
    private Label         countBadge;


    // ── Constructor  (matches HomePage.openQuizzesPage() call exactly) ────────
    public MyQuizzesPanel(User user, Runnable onBack, Consumer<Integer> onTakeQuiz) {
        this.currentUser = user;
        this.onBack      = onBack;
        this.onTakeQuiz  = onTakeQuiz;
        build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ROOT
    // ─────────────────────────────────────────────────────────────────────────

    private void build() {
        // Clip this StackPane to its own bounds so blobs/grid lines
        // can never bleed left over the sidebar and block its buttons.
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        // Gradient background — same as TakeQuizPanel
        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web(C_BG_A)),
                        new Stop(1, Color.web(C_BG_B))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().add(buildDecoLayer());

        VBox main = new VBox(0);
        VBox.setVgrow(main, Priority.ALWAYS);
        main.setMaxWidth(Double.MAX_VALUE);

        ScrollPane body = buildBody();
        VBox.setVgrow(body, Priority.ALWAYS);

        main.getChildren().addAll(buildTopBar(), body);
        getChildren().add(main);

        // Load data after UI tree is ready
        allRows = controller.loadRows(currentUser);
        renderRows(allRows);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BACKGROUND DECORATION  (grid + blobs, same as TakeQuizPanel)
    // ─────────────────────────────────────────────────────────────────────────

    private Pane buildDecoLayer() {
        Pane p = new Pane();
        p.setPickOnBounds(false);
        p.setMouseTransparent(true);

        for (int x = 0; x < 1600; x += 80) {
            Rectangle r = new Rectangle(x, 0, 1, 1200);
            r.setFill(Color.web("#0FAFDD", 0.06));
            p.getChildren().add(r);
        }
        for (int y = 0; y < 1200; y += 80) {
            Rectangle r = new Rectangle(0, y, 1600, 1);
            r.setFill(Color.web("#0FAFDD", 0.06));
            p.getChildren().add(r);
        }
        p.getChildren().addAll(
                blob(80,   60,  180, "#0FAFDD", 0.07),
                blob(1150, 90,  220, "#6366F1", 0.055),
                blob(280,  580, 160, "#60A5FA", 0.05),
                blob(1020, 620, 150, "#818CF8", 0.045)
        );
        return p;
    }

    private Circle blob(double x, double y, double r, String c, double op) {
        Circle circle = new Circle(r);
        circle.setFill(Color.web(c, op));
        circle.setTranslateX(x);
        circle.setTranslateY(y);
        circle.setEffect(new GaussianBlur(55));
        return circle;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TOP BAR  (matches TakeQuizPanel header style exactly)
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 28, 14, 28));
        bar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-border-color: rgba(15,175,221,0.15); -fx-border-width: 0 0 1 0;");
        bar.setEffect(new DropShadow(8, Color.rgb(30, 58, 138, 0.06)));

        // Back button — identical style to TakeQuizPanel.btnBack
        Button backBtn = new Button("← Back");
        String backNorm =
                "-fx-background-color: transparent; -fx-text-fill: " + C_BLUE + ";" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: 700; -fx-font-size: 13;" +
                        "-fx-cursor: hand; -fx-padding: 6 14; -fx-background-radius: 999;" +
                        "-fx-border-color: rgba(15,175,221,0.40); -fx-border-width: 1; -fx-border-radius: 999;";
        String backHov =
                "-fx-background-color: rgba(15,175,221,0.10); -fx-text-fill: " + C_BLUE + ";" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: 700; -fx-font-size: 13;" +
                        "-fx-cursor: hand; -fx-padding: 6 14; -fx-background-radius: 999;" +
                        "-fx-border-color: " + C_BLUE + "; -fx-border-width: 1.5; -fx-border-radius: 999;";
        backBtn.setStyle(backNorm);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(backHov));
        backBtn.setOnMouseExited(e  -> backBtn.setStyle(backNorm));
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        // Title
        VBox titles = new VBox(2);
        Label titleLbl = new Label("📋  My Quizzes");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        titleLbl.setTextFill(Color.web(C_NAVY));

        String name = (currentUser != null) ? currentUser.getDisplayName() : "";
        Label subLbl = new Label("Quiz history and application statuses for " + name);
        subLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:12; -fx-text-fill:" + C_SLATE + ";");
        titles.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Count badge — same visual as TakeQuizPanel's lblStatus
        countBadge = new Label("—");
        countBadge.setStyle(
                "-fx-background-color: rgba(15,175,221,0.12); -fx-background-radius: 999;" +
                        "-fx-border-radius: 999; -fx-border-color: rgba(15,175,221,0.35); -fx-border-width: 1;" +
                        "-fx-text-fill: " + C_INDIG + "; -fx-font-size: 11; -fx-font-weight: 700;" +
                        "-fx-font-family: 'Segoe UI'; -fx-padding: 5 16;");

        bar.getChildren().addAll(backBtn, titles, spacer, countBadge);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BODY  (filter bar + column header + scrollable list)
    // ─────────────────────────────────────────────────────────────────────────

    private ScrollPane buildBody() {
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 28, 24, 28));

        body.getChildren().addAll(buildColHeader());

        listBox = new VBox(10);
        body.getChildren().add(listBox);

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent;" +
                        "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  COLUMN HEADER STRIP
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildColHeader() {
        HBox h = new HBox(0);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(9, 22, 9, 28));
        h.setStyle(
                "-fx-background-color: rgba(255,255,255,0.55);" +
                        "-fx-background-radius:12; -fx-border-radius:12;" +
                        "-fx-border-color: rgba(59,130,246,0.12); -fx-border-width:1;");

        h.getChildren().addAll(
                ch("JOB OFFER",           280),
                ch("REQUIRED SKILLS",     190),
                ch("QUIZ STATUS",         120),
                ch("SCORE",               90),
                ch("RESULT",              120),
                ch("ACTION",              130)
        );
        return h;
    }

    private Label ch(String text, double w) {
        Label l = new Label(text);
        l.setPrefWidth(w); l.setMinWidth(w);
        l.setStyle("-fx-font-family:'Segoe UI'; -fx-font-weight:700; -fx-font-size:10;" +
                "-fx-text-fill:" + C_SLATE + ";");
        return l;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RENDER ROWS
    // ─────────────────────────────────────────────────────────────────────────

    private void renderRows(List<QuizRow> rows) {
        listBox.getChildren().clear();

        int shown = rows.size();
        countBadge.setText(shown + " accepted application" + (shown != 1 ? "s" : ""));

        if (rows.isEmpty()) {
            listBox.getChildren().add(buildEmptyState());
            return;
        }

        for (QuizRow row : rows) {
            listBox.getChildren().add(buildRowCard(row));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INDIVIDUAL ROW CARD
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildRowCard(QuizRow row) {

        // Accent-bar colour driven by candidature status

        // ── Left accent bar ───────────────────────────────────────────────────
        Rectangle accent = new Rectangle(5, 64);
        accent.setArcWidth(5); accent.setArcHeight(5);
        HBox accentWrap = new HBox(accent);
        accentWrap.setAlignment(Pos.CENTER);
        accentWrap.setPadding(new Insets(0, 14, 0, 14));

        // ── 1. JOB OFFER NAME ─────────────────────────────────────────────────
        VBox jobCol = col(280);
        Label jobTitle = new Label(row.jobTitle());
        jobTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        jobTitle.setTextFill(Color.web(C_NAVY));
        jobTitle.setMaxWidth(264);
        jobTitle.setEllipsisString("…");
        Label jobId = new Label("Job #" + row.jobOfferId());
        jobCol.getChildren().addAll(jobTitle, jobId);

        // ── 2. CANDIDATURE STATUS ─────────────────────────────────────────────


        // ── 3. REQUIRED SKILLS ────────────────────────────────────────────────
        VBox skillsCol = col(190);
        String skills = row.requiredSkills();
        if (skills != null && !skills.isBlank()) {
            HBox tags = new HBox(5);
            tags.setAlignment(Pos.CENTER_LEFT);
            String[] parts = skills.split("[,;]+");
            int shown = 0;
            for (String part : parts) {
                String t = part.trim();
                if (t.isEmpty() || shown >= 3) continue;
                Label tag = new Label(t);
                tag.setStyle(
                        "-fx-background-color:rgba(15,175,221,0.12); -fx-text-fill:" + C_INDIG + ";" +
                                "-fx-font-family:'Segoe UI'; -fx-font-size:10; -fx-font-weight:600;" +
                                "-fx-background-radius:999; -fx-padding:3 9;");
                tags.getChildren().add(tag);
                shown++;
            }
            if (parts.length > 3) {
                Label more = new Label("+" + (parts.length - 3));
                more.setStyle(
                        "-fx-background-color:#F1F5F9; -fx-text-fill:" + C_SLATE + ";" +
                                "-fx-font-family:'Segoe UI'; -fx-font-size:10; -fx-font-weight:600;" +
                                "-fx-background-radius:999; -fx-padding:3 9;");
                tags.getChildren().add(more);
            }
            skillsCol.getChildren().add(tags);
        } else {
            skillsCol.getChildren().add(dash());
        }

        // ── 4. QUIZ STATUS BADGE ──────────────────────────────────────────────
        VBox quizCol = col(120);
        String quizBg, quizFg, quizTxt;
        if (!row.quizTaken()) {
            quizBg = "#F1F5F9"; quizFg = C_SLATE; quizTxt = "Not Taken";
        } else if (Boolean.TRUE.equals(row.quizPassed())) {
            quizBg = "rgba(16,185,129,0.13)"; quizFg = C_GREEN; quizTxt = "Passed ✓";
        } else {
            quizBg = "rgba(239,68,68,0.10)";  quizFg = C_ROSE;  quizTxt = "Failed ✗";
        }
        quizCol.getChildren().add(badge(quizTxt, quizBg, quizFg));

        // ── 5. SCORE ──────────────────────────────────────────────────────────
        VBox scoreCol = col(90);
        if (row.quizTaken() && row.scorePercent() != null) {
            double pct = row.scorePercent().doubleValue();
            String scoreColor = pct >= 80 ? C_GREEN : pct >= 50 ? C_AMBER : C_ROSE;

            Label scoreLbl = new Label(row.scoreLabel());
            scoreLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-weight:800; -fx-font-size:17;" +
                    "-fx-text-fill:" + scoreColor + ";");

            // Mini progress bar
            StackPane barBg = new StackPane();
            barBg.setPrefSize(72, 5); barBg.setMaxWidth(72);
            barBg.setStyle("-fx-background-color:#E5E7EB; -fx-background-radius:999;");
            Region barFill = new Region();
            barFill.setPrefSize(Math.max(4, pct / 100.0 * 72), 5);
            barFill.setStyle("-fx-background-color:" + scoreColor + "; -fx-background-radius:999;");
            barBg.setAlignment(Pos.CENTER_LEFT);
            barBg.getChildren().add(barFill);

            scoreCol.getChildren().addAll(scoreLbl, barBg);
        } else {
            scoreCol.getChildren().add(dash());
        }

        // ── 6. RESULT BADGE ───────────────────────────────────────────────────
        VBox resultCol = col(120);
        if (row.quizTaken() && row.quizPassed() != null) {
            boolean passed = row.quizPassed();
            resultCol.getChildren().add(badge(
                    passed ? "🏆  Passed" : "❌  Failed",
                    passed ? "rgba(16,185,129,0.13)" : "rgba(239,68,68,0.10)",
                    passed ? C_GREEN : C_ROSE));
        } else {
            resultCol.getChildren().add(dash());
        }

        // ── 7. ACTION ─────────────────────────────────────────────────────────
        VBox actionCol = col(130);

        boolean canTake =  !row.quizTaken();

        if (canTake) {
            // Blue gradient "Take Quiz" — only reachable path when eligible
            Button takeBtn = new Button("▶  Take Quiz");
            takeBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right," + C_BLUE + "," + C_INDIG + ");" +
                            "-fx-text-fill:white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                            "-fx-font-size:12; -fx-background-radius:20; -fx-padding:9 16; -fx-cursor:hand;");
            takeBtn.setEffect(new DropShadow(8, Color.web(C_BLUE, 0.30)));
            takeBtn.setOnMouseEntered(ev -> takeBtn.setEffect(new DropShadow(14, Color.web(C_BLUE, 0.45))));
            takeBtn.setOnMouseExited(ev  -> takeBtn.setEffect(new DropShadow(8,  Color.web(C_BLUE, 0.30))));
            takeBtn.setOnAction(ev -> { if (onTakeQuiz != null) onTakeQuiz.accept(row.jobOfferId()); });
            actionCol.getChildren().add(takeBtn);

        } else {
            Label done = new Label(Boolean.TRUE.equals(row.quizPassed()) ? "✔  Completed" : "✗  Completed");
            done.setStyle("-fx-font-family:'Segoe UI'; -fx-font-weight:600; -fx-font-size:12;" +
                    "-fx-text-fill:" + (Boolean.TRUE.equals(row.quizPassed()) ? C_GREEN : C_SLATE) + ";");
            actionCol.getChildren().add(done);

        }

        // ── Assemble card ─────────────────────────────────────────────────────
        String styleNorm =
                "-fx-background-color:" + C_CARD + ";" +
                        "-fx-background-radius:18; -fx-border-radius:18;" +
                        "-fx-border-color:rgba(59,130,246,0.13); -fx-border-width:1;";
        String styleHov =
                "-fx-background-color:rgba(255,255,255,0.97);" +
                        "-fx-background-radius:18; -fx-border-radius:18;" +
                        "-fx-border-color:rgba(59,130,246,0.3); -fx-border-width:1.5;" +
                        "-fx-scale-x:1.003; -fx-scale-y:1.003;";

        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 0));
        card.setStyle(styleNorm);
        card.setEffect(new DropShadow(12, Color.web("#0c4a6e", 0.07)));

        card.setOnMouseEntered(e -> card.setStyle(styleHov));
        card.setOnMouseExited(e  -> card.setStyle(styleNorm));

        card.getChildren().addAll(
                accentWrap, jobCol, skillsCol, quizCol, scoreCol, resultCol, actionCol);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  EMPTY STATE
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildEmptyState() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(80, 0, 80, 0));

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size:54px;");

        Label title = new Label("No applications found");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(C_NAVY));

        String subTxt = "You haven't been accepted to any jobs yet.";
        Label sub = new Label(subTxt);
        sub.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:13; -fx-text-fill:" + C_SLATE + ";");

        box.getChildren().addAll(icon, title, sub);
        return box;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Fixed-width aligned column container */
    private VBox col(double w) {
        VBox v = new VBox(5);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setPrefWidth(w); v.setMinWidth(w); v.setMaxWidth(w);
        v.setPadding(new Insets(0, 12, 0, 0));
        return v;
    }

    /** Coloured pill badge */
    private Label badge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-background-color:" + bg + "; -fx-text-fill:" + fg + ";" +
                        "-fx-font-family:'Segoe UI'; -fx-font-weight:700; -fx-font-size:12;" +
                        "-fx-background-radius:20; -fx-padding:5 12;");
        return l;
    }

    /** Em-dash placeholder for empty cells */
    private Label dash() {
        Label l = new Label("—");
        l.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:14; -fx-text-fill:#CBD5E1;");
        return l;
    }

    private String candidatureColor(String status) {
        if (status == null) return C_AMBER;
        return switch (status.toLowerCase()) {
            case "accepted" -> C_GREEN;
            case "rejected" -> C_ROSE;
            default         -> C_AMBER;
        };
    }

    private String candidatureBg(String status) {
        if (status == null) return "rgba(245,158,11,0.12)";
        return switch (status.toLowerCase()) {
            case "accepted" -> "rgba(16,185,129,0.13)";
            case "rejected" -> "rgba(239,68,68,0.10)";
            default         -> "rgba(245,158,11,0.12)";
        };
    }
}