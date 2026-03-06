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
import Service.AiCandidateAnalysisService;
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
    private final AiCandidateAnalysisService analysisService = new AiCandidateAnalysisService();

    private static final String SOURCE_LANG   = "en";
    private static final int    SECS_PER_Q    = 60;   // 1 minute per question

    // ── State ─────────────────────────────────────────────────────────────────
    private Timeline timer;
    private int      remainingSeconds = 0;

    private BigDecimal lastPercent  = null;
    private int        lastCorrect  = 0;
    private int        lastTotal    = 0;
    private String     lastJobTitle = "";
    private String     lastSkills   = "";  // required_skills from job_offer, for AI analysis

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
                lastSkills = (reqSkills != null) ? reqSkills : "";
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
        final BigDecimal fPercent = lastPercent;
        final String fSkills = lastSkills;
        // Capture stage NOW while view is still attached to a scene
        javafx.stage.Stage owner = (view.getScene() != null)
                ? (javafx.stage.Stage) view.getScene().getWindow() : null;
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage w = owner;
            if (w == null && view.getScene() != null)
                w = (javafx.stage.Stage) view.getScene().getWindow();
            if (w == null) return;  // scene gone — silently skip popup
            ui.QuizResultPopup.show(w, lastJobTitle, currentUserId, currentJobOfferId,
                    fPercent, lastTotal, lastCorrect, fTimesUp,
                    () -> view.btnBack.fire(),
                    // AI analysis callback — only triggered if user clicks the button (score < 50)
                    (p, s) -> runAiRemediation(p, fSkills));
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

    // ── AI Remediation ────────────────────────────────────────────────────────

    /**
     * Runs Ollama AI analysis on a background thread, then shows the result dialog.
     * Called when the candidate scored below 50% and clicks "Get AI Analysis" in the popup.
     */
    void runAiRemediation(BigDecimal percent, String skills) {
        // Show a loading indicator while AI is thinking
        javafx.stage.Stage loadingStage = new javafx.stage.Stage();
        loadingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        loadingStage.setTitle("AI Analysis");
        javafx.scene.control.Label loadingLbl = new javafx.scene.control.Label("🤖  Analyzing your results...\nThis may take a few seconds.");
        loadingLbl.setStyle("-fx-font-size:14; -fx-font-family:'Segoe UI'; -fx-text-fill:#334155; -fx-padding:30;");
        loadingLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        loadingLbl.setWrapText(true);
        loadingStage.setScene(new javafx.scene.Scene(new javafx.scene.layout.StackPane(loadingLbl), 340, 130));
        loadingStage.show();

        String prompt =
                "YOUR RESPONSE MUST START WITH '{' — NO exceptions, no preamble, no 'Sure', no explanation.\n" +
                        "Output ONLY a single raw JSON object. Nothing before '{'. Nothing after '}'.\n\n" +
                        "Context:\n" +
                        "- Candidate score: " + percent.toPlainString() + "% (FAILED, under 50%)\n" +
                        "- Required job skills: " + (skills == null || skills.isBlank() ? "General technical skills" : skills) + "\n\n" +
                        "Fill in this exact JSON (replace the placeholders):\n" +
                        "{\n" +
                        "  \"strength_summary\": \"...\",\n" +
                        "  \"weakness_summary\": \"...\",\n" +
                        "  \"hire_recommendation\": \"Borderline\",\n" +
                        "  \"reasoning\": \"...\",\n" +
                        "  \"resources\": [\n" +
                        "    {\"title\": \"...\", \"type\": \"video\", \"url\": \"https://...\", \"why\": \"...\"},\n" +
                        "    {\"title\": \"...\", \"type\": \"docs\",  \"url\": \"https://...\", \"why\": \"...\"}\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Rules:\n" +
                        "- hire_recommendation must be exactly one of: Hire, Borderline, No hire\n" +
                        "- resources: 6-10 items, at least 3 videos and 2 official docs, all with real URLs\n" +
                        "- Do NOT wrap in markdown. Do NOT add any text outside the JSON object.";

        Thread t = new Thread(() -> {
            try {
                String json = analysisService.analyze(prompt);

                // Debug: print raw JSON to console so you can see what the model returned
                System.out.println("[AI RAW JSON]\n" + json);

                // Normalize common alternative key names the model might use instead of the expected ones
                json = json
                        .replace("\"strengths\":",           "\"strength_summary\":")
                        .replace("\"weaknesses\":",          "\"weakness_summary\":")
                        .replace("\"strength\":",            "\"strength_summary\":")
                        .replace("\"weakness\":",            "\"weakness_summary\":")
                        .replace("\"recommendation\":",      "\"hire_recommendation\":")
                        .replace("\"reason\":",              "\"reasoning\":")
                        .replace("\"learning_resources\":",  "\"resources\":")
                        .replace("\"suggested_resources\":", "\"resources\":")
                        .replace("\"link\":",                "\"url\":")
                        .replace("\"description\":",         "\"why\":");

                com.google.gson.Gson gson = new com.google.gson.Gson();
                dto.AiCandidateAnalysisDTO result = gson.fromJson(json, dto.AiCandidateAnalysisDTO.class);
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAnalysisDialog(result);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    error("AI Analysis Error", "Could not get AI analysis:\n" + ex.getMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void showAnalysisDialog(dto.AiCandidateAnalysisDTO a) {
        // ── Colours ───────────────────────────────────────────────────────────
        String BG        = "#f0f8ff";   // alice blue page bg
        String CARD_BG   = "#ffffff";
        String CARD_BD   = "#bfdbfe";   // blue-200
        String ACCENT    = "#3b82f6";   // blue-500
        String ACCENT2   = "#0ea5e9";   // sky-500
        String HEAD_BG   = "linear-gradient(to right,#3b82f6,#0ea5e9)";
        String TEXT_DARK = "#1e3a5f";
        String TEXT_MID  = "#475569";
        String TEXT_LITE = "#64748b";

        // ── Root scroll ───────────────────────────────────────────────────────
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(16);
        root.setStyle("-fx-background-color:" + BG + "; -fx-padding:0;");
        root.setPrefWidth(680);

        // ── Header banner ─────────────────────────────────────────────────────
        javafx.scene.layout.StackPane header = new javafx.scene.layout.StackPane();
        header.setStyle("-fx-background-color:" + HEAD_BG + "; -fx-padding:22 28;");
        javafx.scene.layout.VBox hContent = new javafx.scene.layout.VBox(4);
        hContent.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.control.Label hTitle = new javafx.scene.control.Label("🤖  AI Candidate Analysis");
        hTitle.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:20; -fx-font-weight:800; -fx-text-fill:white;");
        javafx.scene.control.Label hSub = new javafx.scene.control.Label("Personalized insights based on your quiz performance");
        hSub.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:12; -fx-text-fill:rgba(255,255,255,0.82);");
        hContent.getChildren().addAll(hTitle, hSub);
        header.getChildren().add(hContent);

        // ── Body padding wrapper ───────────────────────────────────────────────
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(14);
        body.setStyle("-fx-padding:18 24 10 24; -fx-background-color:" + BG + ";");

        // ── Helper: section card ───────────────────────────────────────────────
        // We'll build cards inline below

        // ── Strengths card ────────────────────────────────────────────────────
        body.getChildren().add(sectionCard("💪  Strengths", a.strength_summary,
                "#dcfce7", "#16a34a", "#14532d"));

        // ── Weaknesses card ───────────────────────────────────────────────────
        body.getChildren().add(sectionCard("⚠️  Weaknesses", a.weakness_summary,
                "#fef9c3", "#ca8a04", "#713f12"));

        // ── Hire recommendation pill row ──────────────────────────────────────
        javafx.scene.layout.HBox recRow = new javafx.scene.layout.HBox(12);
        recRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String recText  = a.hire_recommendation != null ? a.hire_recommendation : "—";
        String recBg    = recText.equalsIgnoreCase("Hire")       ? "#dcfce7" :
                recText.equalsIgnoreCase("Borderline") ? "#fef9c3" : "#fee2e2";
        String recColor = recText.equalsIgnoreCase("Hire")       ? "#16a34a" :
                recText.equalsIgnoreCase("Borderline") ? "#ca8a04" : "#dc2626";

        javafx.scene.control.Label recLabel = new javafx.scene.control.Label("🏅  Recommendation");
        recLabel.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:13; -fx-font-weight:700; -fx-text-fill:" + TEXT_DARK + ";");
        javafx.scene.control.Label recBadge = new javafx.scene.control.Label(recText);
        recBadge.setStyle("-fx-background-color:" + recBg + "; -fx-text-fill:" + recColor + ";" +
                "-fx-font-family:'Segoe UI'; -fx-font-weight:800; -fx-font-size:13;" +
                "-fx-background-radius:999; -fx-padding:5 16;");
        recRow.getChildren().addAll(recLabel, recBadge);

        // ── Reasoning card ────────────────────────────────────────────────────
        body.getChildren().add(recRow);
        body.getChildren().add(sectionCard("📝  Reasoning", a.reasoning,
                "#eff6ff", ACCENT, TEXT_DARK));

        // ── Resources ─────────────────────────────────────────────────────────
        if (a.resources != null && !a.resources.isEmpty()) {
            javafx.scene.control.Label resTitle = new javafx.scene.control.Label("📚  Learning Resources");
            resTitle.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:15; -fx-font-weight:800; -fx-text-fill:" + TEXT_DARK + ";");
            body.getChildren().add(resTitle);

            for (int i = 0; i < a.resources.size(); i++) {
                var r = a.resources.get(i);

                String typeColor = switch (r.type == null ? "" : r.type.toLowerCase()) {
                    case "video"   -> "#7c3aed";
                    case "docs"    -> "#0369a1";
                    case "course"  -> "#0891b2";
                    default        -> "#475569";
                };
                String typeBg = switch (r.type == null ? "" : r.type.toLowerCase()) {
                    case "video"   -> "#ede9fe";
                    case "docs"    -> "#e0f2fe";
                    case "course"  -> "#cffafe";
                    default        -> "#f1f5f9";
                };

                javafx.scene.layout.VBox resCard = new javafx.scene.layout.VBox(5);
                resCard.setStyle("-fx-background-color:" + CARD_BG + ";" +
                        "-fx-background-radius:12; -fx-border-radius:12;" +
                        "-fx-border-color:" + CARD_BD + "; -fx-border-width:1;" +
                        "-fx-padding:12 16;");

                // Top row: number + type badge + title
                javafx.scene.layout.HBox topRow = new javafx.scene.layout.HBox(8);
                topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                javafx.scene.control.Label numLbl = new javafx.scene.control.Label(String.valueOf(i + 1));
                numLbl.setStyle("-fx-background-color:" + ACCENT + "; -fx-text-fill:white;" +
                        "-fx-font-size:11; -fx-font-weight:800; -fx-font-family:'Segoe UI';" +
                        "-fx-background-radius:999; -fx-min-width:22; -fx-min-height:22;" +
                        "-fx-alignment:center; -fx-padding:0 6;");

                javafx.scene.control.Label typeLbl = new javafx.scene.control.Label(
                        r.type != null ? r.type.toUpperCase() : "LINK");
                typeLbl.setStyle("-fx-background-color:" + typeBg + "; -fx-text-fill:" + typeColor + ";" +
                        "-fx-font-size:10; -fx-font-weight:800; -fx-font-family:'Segoe UI';" +
                        "-fx-background-radius:999; -fx-padding:2 8;");

                javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(
                        r.title != null ? r.title : "Resource");
                titleLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:13;" +
                        "-fx-font-weight:700; -fx-text-fill:" + TEXT_DARK + ";");
                titleLbl.setWrapText(true);
                javafx.scene.layout.HBox.setHgrow(titleLbl, javafx.scene.layout.Priority.ALWAYS);

                topRow.getChildren().addAll(numLbl, typeLbl, titleLbl);

                // URL row
                javafx.scene.control.Label urlLbl = new javafx.scene.control.Label(
                        "🔗  " + (r.url != null ? r.url : ""));
                urlLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:11;" +
                        "-fx-text-fill:" + ACCENT2 + "; -fx-cursor:hand;");
                urlLbl.setWrapText(true);
                // Click to open in browser
                if (r.url != null && !r.url.isBlank()) {
                    final String fUrl = r.url;
                    urlLbl.setOnMouseClicked(ev -> {
                        try { java.awt.Desktop.getDesktop().browse(new java.net.URI(fUrl)); }
                        catch (Exception ignored) {}
                    });
                }

                // Why row
                javafx.scene.control.Label whyLbl = new javafx.scene.control.Label(
                        "💡  " + (r.why != null ? r.why : ""));
                whyLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:11; -fx-text-fill:" + TEXT_LITE + ";");
                whyLbl.setWrapText(true);

                resCard.getChildren().addAll(topRow, urlLbl, whyLbl);
                body.getChildren().add(resCard);
            }
        }

        // Spacer at bottom
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(8);
        body.getChildren().add(spacer);

        root.getChildren().addAll(header, body);

        // ── Scroll pane ───────────────────────────────────────────────────────
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:" + BG + "; -fx-background:transparent;" +
                "-fx-border-color:transparent;");
        scroll.setPrefViewportHeight(560);
        scroll.setPrefWidth(700);

        // ── Dialog ────────────────────────────────────────────────────────────
        javafx.scene.control.Dialog<Void> d = new javafx.scene.control.Dialog<>();
        d.setTitle("AI Analysis");
        d.getDialogPane().setHeader(null);
        d.getDialogPane().setStyle("-fx-background-color:" + BG + "; -fx-padding:0;");
        d.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        d.getDialogPane().setContent(scroll);

        // Style the close button
        d.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE)
                .setStyle("-fx-background-color:" + ACCENT + "; -fx-text-fill:white;" +
                        "-fx-font-family:'Segoe UI'; -fx-font-weight:800; -fx-font-size:13;" +
                        "-fx-background-radius:999; -fx-padding:8 24; -fx-cursor:hand;");

        d.showAndWait();
    }

    /** Builds a titled content card with coloured left border. */
    private javafx.scene.layout.VBox sectionCard(String title, String content,
                                                 String bgColor, String accentColor,
                                                 String textColor) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(6);
        card.setStyle("-fx-background-color:" + bgColor + ";" +
                "-fx-background-radius:12; -fx-border-radius:12;" +
                "-fx-border-color:" + accentColor + "44; -fx-border-width:0 0 0 4;" +
                "-fx-padding:12 16;");

        javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(title);
        titleLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:13; -fx-font-weight:800;" +
                "-fx-text-fill:" + accentColor + ";");

        javafx.scene.control.Label contentLbl = new javafx.scene.control.Label(
                content != null ? content : "—");
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:13;" +
                "-fx-text-fill:" + textColor + "; -fx-line-spacing:2;");

        card.getChildren().addAll(titleLbl, contentLbl);
        return card;
    }}