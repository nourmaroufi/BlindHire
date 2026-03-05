package Controller;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import Model.choiceqcm;
import Model.question;
import Service.choiceqcmService;
import Service.questionService;
import Service.scoreService;
import ui.Navigator;
import ui.TakeQuizPanel;
import Utils.Mydb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import Service.TranslationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class QuizPassController {

    private final TakeQuizPanel view;

    private final questionService  questionService  = new questionService();
    private final choiceqcmService choiceService    = new choiceqcmService();
    private final scoreService     scoreService     = new scoreService();
    private final TranslationService translationService = new TranslationService();

    private static final String SOURCE_LANG   = "en";
    private static final int    SECS_PER_Q    = 60;   // 1 minute per question

    // ── State ─────────────────────────────────────────────────────────────────
    private Timeline timer;
    private int      remainingSeconds = 0;

    private BigDecimal lastPercent  = null;
    private int        lastCorrect  = 0;
    private int        lastTotal    = 0;
    private String     lastJobTitle = "";

    // Per-question answer tracking: questionId → chosen choiceqcm (null = skipped)
    private final Map<Integer, choiceqcm> answers = new LinkedHashMap<>();

    // Translated questions + choices ready to display
    private record QEntry(question q, List<choiceqcm> choices,
                          String translatedQ, List<String> translatedChoices) {}

    private List<QEntry> entries         = new ArrayList<>();
    private int          currentIndex    = 0;   // 0-based index of shown question
    private int          currentJobOfferId = 0;
    private int          currentUserId   = 0;
    private boolean      submitted       = false; // guard against double-submit

    // Dynamic nav buttons (created per question display)
    private Button btnNext   = null;
    private Button btnSubmitQ = null;   // shown only on last question

    public QuizPassController(TakeQuizPanel view) { this.view = view; }

    // ── Init ──────────────────────────────────────────────────────────────────
    public void init() {
        view.btnBack.setOnAction(e -> Navigator.showQuizBuilder());
        view.btnLoad.setOnAction(e -> loadQuiz());
        // btnSubmit on TakeQuizPanel is hidden in one-question mode;
        // we use an inline Submit button per question card instead.
        view.btnSubmit.setVisible(false);
        view.btnSubmit.setManaged(false);

        view.langCombo.getItems().setAll(
                new TakeQuizPanel.LangItem("en", "English"),
                new TakeQuizPanel.LangItem("fr", "Français"),
                new TakeQuizPanel.LangItem("es", "Español"),
                new TakeQuizPanel.LangItem("de", "Deutsch"),
                new TakeQuizPanel.LangItem("it", "Italiano"),
                new TakeQuizPanel.LangItem("ar", "العربية"),
                new TakeQuizPanel.LangItem("pt", "Português"),
                new TakeQuizPanel.LangItem("ru", "Русский"),
                new TakeQuizPanel.LangItem("tr", "Türkçe")
        );
        view.langCombo.getSelectionModel().selectFirst();
        view.lblStatus.setText("Enter your User ID and Job Offer ID, then click Load Quiz");
    }

    // ── autoLoad (called from HomePage notification click) ───────────────────
    public void autoLoad(int userId, int jobOfferId) {
        view.userIdField.setText(String.valueOf(userId));
        view.jobOfferIdField.setText(String.valueOf(jobOfferId));
        view.inputRow.setVisible(false);
        view.inputRow.setManaged(false);

        String jobTitle = "Quiz";
        String skills   = "";
        int    qCount   = 0;
        try {
            Connection cnx = Mydb.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT title, required_skills FROM job_offer WHERE id=? LIMIT 1");
            ps.setInt(1, jobOfferId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                jobTitle = rs.getString("title");
                skills   = rs.getString("required_skills");
                if (skills == null) skills = "";
            }
            qCount = new questionService().countquestionsByJobOffer(jobOfferId);
        } catch (Exception ignored) {}

        final String fTitle = jobTitle; final String fSkills = skills; final int fCount = qCount;
        // Array wrapper so lambda can reference welcome (lambdas need effectively-final)
        ui.QuizWelcomePanel[] welcomeRef = new ui.QuizWelcomePanel[1];
        welcomeRef[0] = new ui.QuizWelcomePanel(
                fTitle, fSkills, fCount, SECS_PER_Q,
                () -> {
                    // ── Apply the language the user chose on the welcome screen ──
                    String chosenLang = welcomeRef[0].getSelectedLanguage();
                    for (TakeQuizPanel.LangItem item : view.langCombo.getItems()) {
                        if (item.getCode().equalsIgnoreCase(chosenLang)) {
                            view.langCombo.getSelectionModel().select(item);
                            break;
                        }
                    }
                    view.getChildren().removeIf(n -> n instanceof ui.QuizWelcomePanel);
                    loadQuiz();
                },
                () -> view.btnBack.fire()
        );
        ui.QuizWelcomePanel welcome = welcomeRef[0];
        welcome.prefWidthProperty().bind(view.widthProperty());
        welcome.prefHeightProperty().bind(view.heightProperty());
        view.getChildren().add(welcome);
    }

    // ── Load Quiz ─────────────────────────────────────────────────────────────
    private void loadQuiz() {
        lastPercent = null; lastCorrect = 0; lastTotal = 0;
        answers.clear(); entries.clear(); currentIndex = 0; submitted = false;

        int userId     = parseId(view.userIdField.getText());
        int jobOfferId = parseId(view.jobOfferIdField.getText());
        if (userId <= 0)     { info("User ID required",     "Enter a valid numeric user ID.");   return; }
        if (jobOfferId <= 0) { info("Job Offer ID required", "Enter the job offer ID.");         return; }

        currentUserId     = userId;
        currentJobOfferId = jobOfferId;

        TakeQuizPanel.LangItem lang = view.langCombo.getValue();
        String targetLang = (lang == null) ? "en" : lang.getCode();

        stopTimer();

        // 1. Fetch job info
        try {
            Connection cnx = Mydb.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT title, required_skills FROM job_offer WHERE id=? LIMIT 1");
            ps.setInt(1, jobOfferId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lastJobTitle = rs.getString("title");
                if (lastJobTitle == null) lastJobTitle = "Job #" + jobOfferId;
                String reqSkills = rs.getString("required_skills");
                view.lblJobTitle.setText("📋  " + lastJobTitle);
                view.lblJobTitle.setVisible(true); view.lblJobTitle.setManaged(true);
                if (reqSkills != null && !reqSkills.isBlank()) {
                    view.lblRequiredSkills.setText("🔧  " + reqSkills);
                    view.lblRequiredSkills.setVisible(true); view.lblRequiredSkills.setManaged(true);
                }
            } else {
                info("Not found", "No job offer found with ID " + jobOfferId + "."); return;
            }
        } catch (SQLException ex) { error("DB Error", ex.getMessage()); return; }

        // 2. Already submitted?
        try {
            if (scoreService.hasScore(userId, jobOfferId)) {
                Connection cnx = Mydb.getInstance().getConnection();
                PreparedStatement ps = cnx.prepareStatement(
                        "SELECT score FROM score WHERE id_user=? AND job_offer_id=? " +
                                "ORDER BY created_at DESC LIMIT 1");
                ps.setInt(1, userId); ps.setInt(2, jobOfferId);
                ResultSet rs = ps.executeQuery();
                BigDecimal old = rs.next() ? rs.getBigDecimal("score") : BigDecimal.ZERO;
                view.questionsBox.getChildren().clear();
                view.lblStatus.setText("Already passed: " + old + "%");
                stopTimer(); view.lblTimer.setText("00:00");
                info("Already passed", "You already completed this quiz.\nScore: " + old + "%");
                return;
            }
        } catch (SQLException ex) { error("DB Error", ex.getMessage()); return; }

        // 3. Load + translate questions
        try {
            List<question> questions = questionService.getquestionsByJobOffer(jobOfferId);
            if (questions.isEmpty()) {
                view.lblStatus.setText("No questions for this job offer yet."); return;
            }
            view.lblStatus.setText("Loading & translating...");

            for (question q : questions) {
                List<choiceqcm> choices = choiceService.getChoicesByQuestion(q.getIdQuestion());
                String qText = q.getStatement();
                if (!targetLang.equalsIgnoreCase(SOURCE_LANG)) {
                    try { qText = translationService.translate(qText, SOURCE_LANG, targetLang); }
                    catch (Exception ignored) {}
                }
                List<String> tc = new ArrayList<>();
                for (choiceqcm c : choices) {
                    String ct = c.getChoiceText();
                    if (!targetLang.equalsIgnoreCase(SOURCE_LANG)) {
                        try { ct = translationService.translate(ct, SOURCE_LANG, targetLang); }
                        catch (Exception ignored) {}
                    }
                    tc.add(ct);
                }
                answers.put(q.getIdQuestion(), null); // initialise – null = unanswered
                entries.add(new QEntry(q, choices, qText, tc));
            }

            view.lblStatus.setText("Question 1 / " + entries.size());
            showQuestion(0);

        } catch (SQLException ex) { error("DB Error", ex.getMessage()); }
    }

    // ── Show one question ─────────────────────────────────────────────────────
    private void showQuestion(int index) {
        stopTimer();
        currentIndex = index;
        boolean isLast = (index == entries.size() - 1);
        QEntry  e      = entries.get(index);

        view.lblStatus.setText("Question " + (index + 1) + " / " + entries.size());
        view.lblTimer.setText(formatTime(SECS_PER_Q));

        // Rebuild questionsBox with a single question card
        view.questionsBox.getChildren().clear();
        view.questionsBox.getChildren().add(buildQuestionCard(e, isLast));

        // Start per-question timer
        startQuestionTimer(isLast);
    }

    // ── Question card ─────────────────────────────────────────────────────────
    private VBox buildQuestionCard(QEntry e, boolean isLast) {
        question q = e.q();

        // Progress bar
        int total = entries.size();
        int done  = currentIndex;
        double pct = total == 0 ? 0 : (double) done / total;

        HBox progressRow = new HBox(10);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        Label progressLbl = new Label("Q" + (currentIndex + 1) + " of " + total);
        progressLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:12;" +
                "-fx-text-fill:#6B7280; -fx-font-weight:600;");

        StackPane bar = new StackPane();
        bar.setAlignment(Pos.CENTER_LEFT);
        Region track = new Region();
        track.setPrefHeight(6);
        track.setStyle("-fx-background-color:#E5E7EB; -fx-background-radius:999;");
        Region fill = new Region();
        fill.setPrefHeight(6);
        fill.setStyle("-fx-background-color: linear-gradient(to right,#3B82F6,#6366F1);" +
                "-fx-background-radius:999;");
        fill.prefWidthProperty().bind(bar.widthProperty().multiply(pct));
        bar.getChildren().addAll(track, fill);
        HBox.setHgrow(bar, Priority.ALWAYS);

        // Points badge
        String pts = q.getPoints() == null ? "1.00" : q.getPoints().toPlainString();
        Label ptsBadge = new Label(pts + " pt" + (q.getPoints() != null && q.getPoints().compareTo(BigDecimal.ONE) != 0 ? "s" : ""));
        ptsBadge.setStyle("-fx-background-color:#EEF2FF; -fx-background-radius:999;" +
                "-fx-border-radius:999; -fx-border-color:#C7D2FE; -fx-border-width:1;" +
                "-fx-text-fill:#4F46E5; -fx-font-size:11; -fx-font-weight:700;" +
                "-fx-font-family:'Segoe UI'; -fx-padding:3 10;");

        progressRow.getChildren().addAll(progressLbl, bar, ptsBadge);

        // Question statement
        Label qLbl = new Label(e.translatedQ());
        qLbl.setWrapText(true);
        qLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        qLbl.setTextFill(Color.web("#1e3a8a"));
        qLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-weight:800; -fx-font-size:16;" +
                "-fx-text-fill:#1e3a8a;");

        // Answer choices
        ToggleGroup tg = new ToggleGroup();
        VBox choicesBox = new VBox(8);

        // Restore previous answer if user navigated back (not supported here, but safe)
        choiceqcm prevAnswer = answers.get(q.getIdQuestion());

        for (int i = 0; i < e.choices().size(); i++) {
            choiceqcm c   = e.choices().get(i);
            String    lbl = (i < e.translatedChoices().size())
                    ? e.translatedChoices().get(i) : c.getChoiceText();

            // Custom styled radio row
            HBox optRow = new HBox(12);
            optRow.setAlignment(Pos.CENTER_LEFT);
            optRow.setPadding(new Insets(12, 16, 12, 16));
            optRow.setStyle(
                    "-fx-background-color:#F8FAFF; -fx-background-radius:12;" +
                            "-fx-border-radius:12; -fx-border-color:#DBEAFE; -fx-border-width:1.5;" +
                            "-fx-cursor:hand;");
            optRow.setEffect(new DropShadow(4, Color.web("#3B82F6", 0.04)));

            // Letter badge A B C D…
            String letter = String.valueOf((char) ('A' + i));
            Label badge = new Label(letter);
            badge.setMinSize(28, 28); badge.setMaxSize(28, 28);
            badge.setAlignment(Pos.CENTER);
            badge.setStyle("-fx-background-color:#EFF6FF; -fx-background-radius:999;" +
                    "-fx-text-fill:#3B82F6; -fx-font-weight:800; -fx-font-size:12;");

            RadioButton rb = new RadioButton(lbl);
            rb.setToggleGroup(tg);
            rb.setUserData(c);
            rb.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:13; -fx-text-fill:#1e293b;");
            HBox.setHgrow(rb, Priority.ALWAYS);
            rb.setMaxWidth(Double.MAX_VALUE);

            if (prevAnswer != null && prevAnswer.getIdChoice() == c.getIdChoice()) {
                rb.setSelected(true);
                optRow.setStyle(
                        "-fx-background-color:#EFF6FF; -fx-background-radius:12;" +
                                "-fx-border-radius:12; -fx-border-color:#3B82F6; -fx-border-width:2;" +
                                "-fx-cursor:hand;");
                badge.setStyle("-fx-background-color:#3B82F6; -fx-background-radius:999;" +
                        "-fx-text-fill:white; -fx-font-weight:800; -fx-font-size:12;");
            }

            optRow.getChildren().addAll(badge, rb);

            // Highlight on select
            rb.selectedProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    optRow.setStyle(
                            "-fx-background-color:#EFF6FF; -fx-background-radius:12;" +
                                    "-fx-border-radius:12; -fx-border-color:#3B82F6; -fx-border-width:2;" +
                                    "-fx-cursor:hand;");
                    badge.setStyle("-fx-background-color:#3B82F6; -fx-background-radius:999;" +
                            "-fx-text-fill:white; -fx-font-weight:800; -fx-font-size:12;");
                } else {
                    optRow.setStyle(
                            "-fx-background-color:#F8FAFF; -fx-background-radius:12;" +
                                    "-fx-border-radius:12; -fx-border-color:#DBEAFE; -fx-border-width:1.5;" +
                                    "-fx-cursor:hand;");
                    badge.setStyle("-fx-background-color:#EFF6FF; -fx-background-radius:999;" +
                            "-fx-text-fill:#3B82F6; -fx-font-weight:800; -fx-font-size:12;");
                }
            });

            // Click anywhere on row selects the radio
            optRow.setOnMouseClicked(ev -> rb.setSelected(true));
            choicesBox.getChildren().add(optRow);
        }

        // Save answer when toggle changes
        tg.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) answers.put(q.getIdQuestion(), (choiceqcm) newT.getUserData());
        });

        // ── Navigation button ─────────────────────────────────────────────────
        HBox navRow = new HBox();
        navRow.setAlignment(Pos.CENTER_RIGHT);
        navRow.setPadding(new Insets(4, 0, 0, 0));

        if (isLast) {
            // Submit button on last question
            Button submit = new Button("Submit Quiz  ✓");
            submit.setStyle(
                    "-fx-background-color: linear-gradient(to right,#2563EB,#4F46E5);" +
                            "-fx-text-fill:white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                            "-fx-font-size:14; -fx-background-radius:12; -fx-padding:12 28; -fx-cursor:hand;");
            submit.setEffect(new DropShadow(10, Color.web("#2563EB", 0.3)));
            submit.setOnAction(ev -> finishQuiz(false));
            btnSubmitQ = submit;

            // Optional: skip button still shows "Submit" to enforce finishing
            navRow.getChildren().add(submit);
        } else {
            Button next = new Button("Next Question  →");
            next.setStyle(
                    "-fx-background-color: linear-gradient(to right,#3B82F6,#6366F1);" +
                            "-fx-text-fill:white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                            "-fx-font-size:14; -fx-background-radius:12; -fx-padding:12 28; -fx-cursor:hand;");
            next.setEffect(new DropShadow(10, Color.web("#3B82F6", 0.3)));
            next.setOnAction(ev -> advanceToNext());
            btnNext = next;
            navRow.getChildren().add(next);
        }

        // Card assembly
        VBox card = new VBox(16);
        card.setPadding(new Insets(22, 22, 18, 22));
        card.setStyle(
                "-fx-background-color:white; -fx-background-radius:18;" +
                        "-fx-border-radius:18; -fx-border-color:rgba(59,130,246,0.15); -fx-border-width:1;");
        card.setEffect(new DropShadow(14, Color.web("#1e3a8a", 0.07)));
        card.getChildren().addAll(progressRow, qLbl, choicesBox, navRow);
        return card;
    }

    // ── Advance to next question ──────────────────────────────────────────────
    private void advanceToNext() {
        stopTimer();
        int next = currentIndex + 1;
        if (next < entries.size()) {
            showQuestion(next);
        } else {
            finishQuiz(false);
        }
    }

    // ── Per-question timer ────────────────────────────────────────────────────
    private void startQuestionTimer(boolean isLast) {
        remainingSeconds = SECS_PER_Q;
        view.lblTimer.setText(formatTime(remainingSeconds));

        // Normal border
        view.lblTimer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 999;" +
                        "-fx-border-radius: 999; -fx-border-color: #BFDBFE; -fx-border-width: 2;" +
                        "-fx-padding: 7 16; -fx-font-family: 'Segoe UI'; -fx-font-weight: 800;" +
                        "-fx-font-size: 15; -fx-text-fill: #1e3a8a;");

        timer = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            remainingSeconds--;
            view.lblTimer.setText(formatTime(Math.max(remainingSeconds, 0)));

            // Turn timer red in last 10 seconds
            if (remainingSeconds <= 10 && remainingSeconds > 0) {
                view.lblTimer.setStyle(
                        "-fx-background-color: #FFF1F2; -fx-background-radius: 999;" +
                                "-fx-border-radius: 999; -fx-border-color: #EF4444; -fx-border-width: 2;" +
                                "-fx-padding: 7 16; -fx-font-family: 'Segoe UI'; -fx-font-weight: 800;" +
                                "-fx-font-size: 15; -fx-text-fill: #EF4444;");
            }

            if (remainingSeconds <= 0) {
                stopTimer();
                if (isLast) {
                    finishQuiz(true);  // time ran out on last question → show results
                } else {
                    // Auto-advance: keep unanswered (null already in map), go next
                    javafx.application.Platform.runLater(this::advanceToNext);
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.playFromStart();
    }

    private void stopTimer() {
        if (timer != null) { timer.stop(); timer = null; }
    }

    // ── Finish quiz + show results ────────────────────────────────────────────
    private void finishQuiz(boolean timesUp) {
        if (submitted) return;  // already saved — ignore duplicate call (timer vs button)
        submitted = true;
        stopTimer();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max   = BigDecimal.ZERO;
        int correctCount = 0;

        for (QEntry e : entries) {
            question  q   = e.q();
            BigDecimal pts = (q.getPoints() == null) ? new BigDecimal("1.00") : q.getPoints();
            max = max.add(pts);
            choiceqcm chosen = answers.get(q.getIdQuestion());
            if (chosen != null && chosen.isCorrect()) {
                total = total.add(pts);
                correctCount++;
            }
        }

        BigDecimal percent = max.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : total.multiply(new BigDecimal("100"))
                .divide(max, 2, java.math.RoundingMode.HALF_UP);

        lastPercent = percent;
        lastCorrect = correctCount;
        lastTotal   = entries.size();

        try {
            scoreService.addScore(currentUserId, currentJobOfferId, percent);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        view.lblStatus.setText("Score saved: " + percent + "% (user " + currentUserId + ")");
        view.lblTimer.setText("00:00");

        final boolean fTimesUp = timesUp;
        // Capture stage NOW while view is still attached to a scene
        javafx.stage.Stage owner = (view.getScene() != null)
                ? (javafx.stage.Stage) view.getScene().getWindow() : null;
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage w = owner;
            if (w == null && view.getScene() != null)
                w = (javafx.stage.Stage) view.getScene().getWindow();
            if (w == null) return;  // scene gone — silently skip popup
            ui.QuizResultPopup.show(w, lastJobTitle, currentUserId, currentJobOfferId,
                    lastPercent, lastTotal, lastCorrect, fTimesUp,
                    () -> view.btnBack.fire());
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int parseId(String txt) {
        if (txt == null || txt.isBlank()) return -1;
        try { return Integer.parseInt(txt.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private String formatTime(int totalSec) {
        return String.format("%02d:%02d", totalSec / 60, totalSec % 60);
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}