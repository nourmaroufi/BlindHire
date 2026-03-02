package controller;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.choiceqcm;
import models.question;
import models.skill;
import services.choiceqcmService;
import services.questionService;
import services.scoreService;
import services.skillService;
import ui.Navigator;
import ui.TakeQuizPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import services.TranslationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


public class QuizPassController {

    private final TakeQuizPanel view;

    private final skillService skillService = new skillService();
    private final questionService questionService = new questionService();
    private final choiceqcmService choiceService = new choiceqcmService();
    private final scoreService scoreService = new scoreService();
    private final TranslationService translationService = new TranslationService();
    private static final String SOURCE_LANG = "en"; // assume DB content is in English
    private java.math.BigDecimal lastPercent = null;
    private boolean autoSubmitting = false;

    private Timeline timer;
    private int remainingSeconds = 0;

    // for testing: 60 seconds. later set to 30*60
    private static final int QUIZ_SECONDS = 60;

    private final Map<Integer, ToggleGroup> groupsByquestionId = new HashMap<>();
    private List<question> currentquestions = new ArrayList<>();

    public QuizPassController(TakeQuizPanel view) {
        this.view = view;
    }

    public void init() {
        view.btnBack.setOnAction(e -> Navigator.showQuizBuilder());
        view.btnLoad.setOnAction(e -> loadQuiz());
        view.btnSubmit.setOnAction(e -> {
            try {
                submitQuiz();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        loadskills();
        view.langCombo.getItems().setAll(
                new ui.TakeQuizPanel.LangItem("en", "English"),
                new ui.TakeQuizPanel.LangItem("fr", "Français"),
                new ui.TakeQuizPanel.LangItem("es", "Español"),
                new ui.TakeQuizPanel.LangItem("de", "Deutsch"),
                new ui.TakeQuizPanel.LangItem("it", "Italiano"),
                new ui.TakeQuizPanel.LangItem("ar", "العربية"),
                new ui.TakeQuizPanel.LangItem("pt", "Português"),
                new ui.TakeQuizPanel.LangItem("ru", "Русский"),
                new ui.TakeQuizPanel.LangItem("tr", "Türkçe")
        );
        view.langCombo.getSelectionModel().selectFirst(); // default English
    }

    private void loadskills() {
        try {
            List<skill> skills = skillService.getAllskills();
            view.skillCombo.getItems().setAll(skills);

            view.skillCombo.setCellFactory(cb -> new ListCell<>() {
                @Override protected void updateItem(skill item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            view.skillCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(skill item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });

            view.lblStatus.setText("skills loaded: " + skills.size());
        } catch (SQLException ex) {
            error("DB Error", ex.getMessage());
        }
    }



    private void loadQuiz() {
        lastPercent = null;
        int userId = parseUserId(view.userIdField.getText());
        if (userId <= 0) { info("User ID required", "Enter a valid user id first."); return; }

        skill skill = view.skillCombo.getValue();
        if (skill == null) { info("skill required", "Select a skill."); return; }

        // language (target)
        ui.TakeQuizPanel.LangItem lang = view.langCombo.getValue();
        String targetLang = (lang == null || lang.getCode() == null) ? "en" : lang.getCode();
        stopTimer();
        view.lblTimer.setText(formatTime(QUIZ_SECONDS));

        // 1) already passed check
        try {
            Connection cnx = utils.mydb.getInstance().getConnection();
            String checkSql = "SELECT score FROM score WHERE id_user=? AND id_skill=? LIMIT 1";
            PreparedStatement ps = cnx.prepareStatement(checkSql);
            ps.setInt(1, userId);
            ps.setInt(2, skill.getIdSkill());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                java.math.BigDecimal old = rs.getBigDecimal("score");
                view.questionsBox.getChildren().clear();
                view.lblStatus.setText("Already passed: " + old + "%");
                info("Already passed", "You already passed this quiz.\nScore: " + old + "%");
                stopTimer();
                view.lblTimer.setText("00:00");
                return;
            }
        } catch (SQLException ex) {
            error("DB Error", ex.getMessage());
            return;
        }

        // 2) load questions + translate display text (UI only)
        try {
            currentquestions = questionService.getquestionsBySkill(skill.getIdSkill());
            groupsByquestionId.clear();
            view.questionsBox.getChildren().clear();

            if (currentquestions.isEmpty()) {
                view.lblStatus.setText("No questions for this skill.");
                return;
            }

            // optional: show status while translating
            view.lblStatus.setText("Loading & translating...");

            for (question q : currentquestions) {

                List<choiceqcm> choices = choiceService.getChoicesByQuestion(q.getIdQuestion());

                // Translate question statement (display only)
                String qText = q.getStatement();
                if (!targetLang.equalsIgnoreCase(SOURCE_LANG)) {
                    try {
                        qText = translationService.translate(qText, SOURCE_LANG, targetLang);
                    } catch (Exception ignored) { /* fallback to original */ }
                }

                // Translate each choice (display only)
                java.util.List<String> translatedChoices = new java.util.ArrayList<>();
                for (choiceqcm c : choices) {
                    String ct = c.getChoiceText();
                    if (!targetLang.equalsIgnoreCase(SOURCE_LANG)) {
                        try {
                            ct = translationService.translate(ct, SOURCE_LANG, targetLang);
                        } catch (Exception ignored) { /* fallback */ }
                    }
                    translatedChoices.add(ct);
                }

                VBox card = questionCardTranslated(q, choices, qText, translatedChoices);
                view.questionsBox.getChildren().add(card);
            }

            view.lblStatus.setText("Loaded: " + currentquestions.size() + " questions (" + targetLang + ")");
            startTimer(QUIZ_SECONDS);
        } catch (SQLException ex) {
            error("DB Error", ex.getMessage());
        }
    }
    private void startTimer(int seconds) {
        stopTimer();

        remainingSeconds = seconds;
        view.lblTimer.setText(formatTime(remainingSeconds));

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            view.lblTimer.setText(formatTime(Math.max(remainingSeconds, 0)));

            if (remainingSeconds <= 0) {
                stopTimer();
                autoSubmitting = true;
                view.lblStatus.setText("Time's up — auto submitting...");

                try {
                    submitQuiz(); // saves score + likely sets lblStatus

                    String scoreTxt = (lastPercent == null) ? "N/A" : (lastPercent.toPlainString() + "%");

                    // force final message
                    view.lblStatus.setText("Time's up — score: " + scoreTxt);

                    javafx.application.Platform.runLater(() ->
                            showInfo("Time's up", "Time ran out.\nYour score: " + scoreTxt + "\n(saved to DB)")
                    );

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() ->
                            showError("DB Error", ex.getMessage())
                    );
                } finally {
                    autoSubmitting = false;
                }

              // <- must be your existing submit method
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.playFromStart();
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
    private VBox questionCardTranslated(question q,
                                        List<choiceqcm> choices,
                                        String translatedQuestion,
                                        List<String> translatedChoices) {

        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: rgba(200,215,255,0.8);" +
                        "-fx-padding: 12 12;"
        );

        Label title = new Label("Q" + q.getIdQuestion() + " • " + translatedQuestion);
        title.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: 800; -fx-text-fill: #1D2B5A; -fx-font-size: 14;");

        String ptsVal = (q.getPoints() == null) ? "1.00" : q.getPoints().toString();
        Label pts = new Label("Points: " + ptsVal);
        pts.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #6E7AA8; -fx-font-size: 12;");

        ToggleGroup tg = new ToggleGroup();
        groupsByquestionId.put(q.getIdQuestion(), tg);

        VBox answersBox = new VBox(6);

        for (int i = 0; i < choices.size(); i++) {
            choiceqcm c = choices.get(i);
            String label = (i < translatedChoices.size()) ? translatedChoices.get(i) : c.getChoiceText();

            RadioButton rb = new RadioButton(label);
            rb.setToggleGroup(tg);
            rb.setUserData(c); // IMPORTANT: keep original object for scoring (is_correct)
            rb.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #2A3A74;");
            answersBox.getChildren().add(rb);
        }

        box.getChildren().addAll(title, pts, answersBox);
        return box;
    }
    private VBox questionCard(question q, List<choiceqcm> choices) {
        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: rgba(200,215,255,0.8);" +
                        "-fx-padding: 12 12;"
        );

        Label title = new Label("Q" + q.getIdQuestion() + " • " + q.getStatement());
        title.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: 800; -fx-text-fill: #1D2B5A; -fx-font-size: 14;");

        Label pts = new Label("Points: " + (q.getPoints() == null ? "1.00" : q.getPoints().toString()));
        pts.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #6E7AA8; -fx-font-size: 12;");

        ToggleGroup tg = new ToggleGroup();
        groupsByquestionId.put(q.getIdQuestion(), tg);

        VBox answersBox = new VBox(6);

        for (choiceqcm c : choices) {
            RadioButton rb = new RadioButton(c.getChoiceText());
            rb.setToggleGroup(tg);
            rb.setUserData(c); // store the choiceqcm object
            rb.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #2A3A74;");
            answersBox.getChildren().add(rb);
        }

        box.getChildren().addAll(title, pts, answersBox);
        return box;
    }

    private void submitQuiz() throws SQLException {
        skill skill = view.skillCombo.getValue();
        if (skill == null) { info("skill required", "Select a skill."); return; }
        int userId = parseUserId(view.userIdField.getText());
        if (userId <= 0) { info("User ID required", "Enter a valid integer user id."); return; }
        if (currentquestions.isEmpty()) { info("No quiz", "Load a quiz first."); return; }
        stopTimer();

        // Compute score
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;

        for (question q : currentquestions) {
            BigDecimal pts = (q.getPoints() == null) ? new BigDecimal("1.00") : q.getPoints();
            max = max.add(pts);

            ToggleGroup tg = groupsByquestionId.get(q.getIdQuestion());
            if (tg == null || tg.getSelectedToggle() == null) continue;

            choiceqcm chosen = (choiceqcm) tg.getSelectedToggle().getUserData();
            if (chosen != null && chosen.isCorrect()) {
                total = total.add(pts);
            }
        }

        BigDecimal percent;
        if (max.compareTo(BigDecimal.ZERO) == 0) {
            percent = BigDecimal.ZERO;
        } else {
            percent = total.multiply(new BigDecimal("100"))
                    .divide(max, 2, java.math.RoundingMode.HALF_UP);
            lastPercent = percent;// 2 decimals
        }


        scoreService.addScore(userId, skill.getIdSkill(), percent);
        if (!autoSubmitting) {
            view.lblStatus.setText("Saved score: " + percent + "% (user " + userId + ")");
            info("Result",
                    "User: " + userId +
                            "\nSkill: " + skill.getName() +
                            "\nScore: " + percent + "%" +
                            "\n(saved to DB)");
        }
        if (lastPercent != null && lastPercent.compareTo(new java.math.BigDecimal("50")) < 0) {
            runAiRemediation(view.skillCombo.getValue(), lastPercent);
        }



    }
    private final services.AiCandidateAnalysisService analysisService = new services.AiCandidateAnalysisService();

    private void runAiRemediation(models.skill sk, java.math.BigDecimal percent) {
        try {
            String prompt =
                    "Return ONLY ONE JSON object. No text before/after. Do NOT say \"Sure\". Do NOT wrap in ``` ." +
                            "Return ONLY JSON. If you output anything other than JSON, you failed." +
                            "Return ONLY ONE valid JSON object. No markdown.\n" +
                            "First char '{' last char '}'.\n\n" +

                            "Context:\n" +
                            "- Candidate score: " + percent.toPlainString() + "% (FAILED, under 50%)\n" +
                            "- Skill: " + sk.getName() + "\n" +
                            "- Skill description: " + (sk.getDescription() == null ? "" : sk.getDescription()) + "\n\n" +

                            "TASK:\n" +
                            "1) strength_summary (1-2 lines)\n" +
                            "2) weakness_summary (2-4 lines)\n" +
                            "3) hire_recommendation: one of [Hire, Borderline, No hire]\n" +
                            "4) reasoning: 2 lines max\n" +
                            "5) resources: give 6 to 10 FREE learning resources WITH DIRECT URLs.\n" +
                            "   Must include at least 3 videos and 2 official docs.\n" +
                            "   Prefer stable sources (official docs, MDN, Microsoft Learn, Oracle, freeCodeCamp, Coursera audit/free, YouTube playlists).\n\n" +

                            "JSON schema:\n" +
                            "{"
                            + "\"strength_summary\":string,"
                            + "\"weakness_summary\":string,"
                            + "\"hire_recommendation\":\"Hire\"|\"Borderline\"|\"No hire\","
                            + "\"reasoning\":string,"
                            + "\"resources\":[{\"title\":string,\"type\":\"video\"|\"docs\"|\"article\"|\"course\",\"url\":string,\"why\":string}]"
                            + "}";

            String json = analysisService.analyze(prompt);

            com.google.gson.Gson gson = new com.google.gson.Gson();
            dto.AiCandidateAnalysisDTO a = gson.fromJson(json, dto.AiCandidateAnalysisDTO.class);

            showAnalysisDialog(a); // same dialog method I gave before
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("AI Error", ex.getMessage());
        }
    }private void showAnalysisDialog(dto.AiCandidateAnalysisDTO a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Strengths:\n").append(a.strength_summary).append("\n\n");
        sb.append("Weaknesses:\n").append(a.weakness_summary).append("\n\n");
        sb.append("Hire recommendation: ").append(a.hire_recommendation).append("\n");
        sb.append("Reasoning: ").append(a.reasoning).append("\n\n");
        sb.append("Resources:\n");

        if (a.resources != null) {
            for (int i = 0; i < a.resources.size(); i++) {
                var r = a.resources.get(i);
                sb.append(i+1).append(". [").append(r.type).append("] ")
                        .append(r.title).append("\n")
                        .append("   ").append(r.url).append("\n")
                        .append("   Why: ").append(r.why).append("\n\n");
            }
        }

        javafx.scene.control.Dialog<Void> d = new javafx.scene.control.Dialog<>();
        d.setTitle("AI Candidate Analysis");
        d.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

        javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(sb.toString());
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setPrefWidth(720);
        ta.setPrefHeight(520);

        d.getDialogPane().setContent(ta);
        d.showAndWait();
    }

    private int parseUserId(String txt) {
        if (txt == null) return -1;
        String t = txt.trim();
        if (t.isEmpty()) return -1;
        try { return Integer.parseInt(t); }
        catch (Exception e) { return -1; }
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
