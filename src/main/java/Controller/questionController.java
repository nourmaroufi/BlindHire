package Controller;

import Service.AiQuizGeneratorService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import Model.choiceqcm;
import Model.question;
import Service.choiceqcmService;
import Service.questionService;
import ui.Navigator;
import ui.QuestionCrudPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class questionController {

    private final QuestionCrudPanel view;
    private final int jobOfferId;         // 0 = standalone, >0 = tied to a job offer
    private final String jobTitle;        // shown in header badge
    private final String requiredSkills;  // comma-separated skills from job offer (replaces skill table)

    private final questionService questionService = new questionService();
    private final choiceqcmService choiceqcmService = new choiceqcmService();
    private final AiQuizGeneratorService aiService = new AiQuizGeneratorService();

    /** Standalone (admin quiz manager – skill combo shown). */
    public questionController(QuestionCrudPanel view) {
        this(view, 0, null, null);
    }

    /** Job-linked (no skill table – questions stored by job_offer_id). */
    public questionController(QuestionCrudPanel view, int jobOfferId, String jobTitle, String requiredSkills) {
        this.view = view;
        this.jobOfferId = jobOfferId;
        this.jobTitle = jobTitle;
        this.requiredSkills = requiredSkills;
    }

    public void init() {
        setupTables();
        bindEvents();
        view.btnLeaderboard.setOnAction(e -> Navigator.showLeaderboard());

        if (jobOfferId > 0) {
            // ── Job-linked mode: no skill table, load questions by jobOfferId ──
            view.lblJobContext.setText("📋  " + (jobTitle != null ? jobTitle : "Job #" + jobOfferId));
            view.lblJobContext.setVisible(true);
            view.lblJobContext.setManaged(true);

            // Show required skills hint
            String skills = (requiredSkills != null && !requiredSkills.isBlank())
                    ? requiredSkills : "not specified";
            view.lblSkillsTag.setText("Required skills: " + skills);

            view.lblStatus.setText("0 questions");

            // Wire finish button — closes the stage
            view.btnFinish.setOnAction(e -> {
                javafx.stage.Stage st = (javafx.stage.Stage) view.getScene().getWindow();
                st.close();
            });

            // Load any questions already saved for this job
            loadQuestionsByJob();
        } else {
            view.lblStatus.setText("Ready");
        }
    }

    // ---------- setup ----------
    private void setupTables() {
        // question columns
        view.colQId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdQuestion()));
        view.colQStatement.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatement()));
        view.colQPoints.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPoints() == null ? "1.00" : d.getValue().getPoints().toString()
        ));
        view.questionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Choice columns
        view.colCId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdChoice()));
        view.colCText.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChoiceText()));
        view.colCCorrect.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isCorrect()));
        view.choiceTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void bindEvents() {

        view.questionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldQ, newQ) -> {
            if (newQ != null) {
                view.statementArea.setText(newQ.getStatement());
                view.pointsField.setText(newQ.getPoints() == null ? "1.00" : newQ.getPoints().toString());
                loadChoicesForquestion(newQ.getIdQuestion());
            } else {
                view.choiceTable.getItems().clear();
            }
        });

        view.choiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldC, newC) -> {
            if (newC != null) view.choiceTextField.setText(newC.getChoiceText());
        });

        // question CRUD
        view.btnAddQuestion.setOnAction(e -> addquestion());
        view.btnUpdateQuestion.setOnAction(e -> updatequestion());
        view.btnDeleteQuestion.setOnAction(e -> deletequestion());
        view.btnClearQuestion.setOnAction(e -> clearquestionForm());

        // Choice CRUD
        view.btnAddChoice.setOnAction(e -> addChoice());
        view.btnUpdateChoice.setOnAction(e -> updateChoiceText());
        view.btnDeleteChoice.setOnAction(e -> deleteChoice());
        view.btnSetCorrect.setOnAction(e -> setCorrectChoice());
        view.btnGoTakeQuiz.setOnAction(e -> ui.Navigator.showTakeQuiz());
        view.btnGenerateQuiz.setOnAction(e -> generateQuizWithAi());


    }

    // ---------- load ----------
    // loadskills() removed — skill table dropped, questions grouped by job_offer_id

    /** Job-linked mode: load questions filtered by job_offer_id (no skill needed). */
    private void loadQuestionsByJob() {
        try {
            List<question> qs = questionService.getquestionsByJobOffer(jobOfferId);
            view.questionTable.getItems().setAll(qs);
            view.lblStatus.setText(qs.size() + " question" + (qs.size() == 1 ? "" : "s"));
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void onskillSelected() {
        // skill table dropped — no-op in standalone mode, use jobOfferId
    }


    private void loadChoicesForquestion(int idquestion) {
        try {
            List<choiceqcm> cs = choiceqcmService.getChoicesByQuestion(idquestion);
            view.choiceTable.getItems().setAll(cs);
            view.lblStatus.setText("Answers: " + cs.size());
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    // ---------- question CRUD ----------
    private void addquestion() {
        // skill table dropped — no skill validation needed

        String statement = safe(view.statementArea.getText());
        if (statement.isEmpty()) { showInfo("Statement required", "Write the question."); return; }

        BigDecimal points = parsePoints(view.pointsField.getText());

        try {
            int newId = questionService.addquestion(statement, points, jobOfferId);
            loadQuestionsByJob();
            selectquestionById(newId);
            view.lblStatus.setText("Added question #" + newId);
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }
    private void generateQuizWithAi() {
        // In job-linked mode, use requiredSkills string as the skill context
        String skillContext = (requiredSkills != null && !requiredSkills.isBlank())
                ? requiredSkills : "general";

        TextInputDialog dialog = new TextInputDialog("Make 5 easy questions, 4 choices each.");
        dialog.setTitle("Generate Quiz (AI)");
        dialog.setHeaderText("Skills: " + skillContext);
        dialog.setContentText("Prompt:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String adminPrompt = result.get().trim();
        int nQ = extractInt(adminPrompt, "(\\d+)\\s*question", 5);
        int nC = extractInt(adminPrompt, "(\\d+)\\s*(answer|choice)", 4);
        if (nC < 2) nC = 2;
        if (adminPrompt.isEmpty()) { showInfo("Prompt required", "Write a prompt."); return; }

        try {
            // IMPORTANT: skill comes from UI, not prompt
            String finalPrompt =
                    "You must return ONLY ONE valid JSON object.\n" +
                            "First char must be '{' and last char must be '}'. No markdown.\n\n" +

                            "TARGET SKILLS: " + skillContext + "\n\n" +

                            "HARD CONSTRAINTS (MUST FOLLOW):\n" +
                            "- Exactly " + nQ + " questions.\n" +
                            "- Each question has exactly " + nC + " choices.\n" +
                            "- Exactly ONE choice has is_correct=true per question.\n" +
                            "- Keep statements short.\n\n" +

                            "JSON SCHEMA:\n" +
                            "{\"skill\":\"" + skillContext + "\",\"questions\":[{\"statement\":string,\"points\":number," +
                            "\"choices\":[{\"text\":string,\"is_correct\":boolean}]}]}\n\n" +

                            "ADMIN NOTES:\n" + adminPrompt + "\n\n" +
                            "Return ONLY the JSON now.";

            String quizJson = aiService.generateQuizJson(finalPrompt);

            com.google.gson.Gson gson = new com.google.gson.Gson();
            dto.AiQuizDTO dtoObj = gson.fromJson(quizJson, dto.AiQuizDTO.class);

            try {
                validateAi(dtoObj, nQ, nC);
            } catch (Exception bad) {
                String retryPrompt = finalPrompt +
                        "\n\nYOU VIOLATED CONSTRAINTS: " + bad.getMessage() +
                        "\nRegenerate correctly. Return ONLY JSON.";

                quizJson = aiService.generateQuizJson(retryPrompt);
                dtoObj = gson.fromJson(quizJson, dto.AiQuizDTO.class);
                validateAi(dtoObj, nQ, nC);
            }

// save after it passes validation
            insertAiQuizIntoDb(0, quizJson);

            showInfo("Done", "Quiz generated and saved.");
            loadQuestionsByJob();


        } catch (Exception ex) {
            ex.printStackTrace();
            showError("AI/DB Error", ex.getMessage());
        }
    }

    private long insertQuestionTx(java.sql.Connection cnx, String statement, java.math.BigDecimal points, int jobOffId) throws Exception {
        String sql = "INSERT INTO question(statement, points, job_offer_id) VALUES (?,?,?)";
        try (java.sql.PreparedStatement ps = cnx.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, statement);
            ps.setBigDecimal(2, points);
            ps.setInt(3, jobOffId);
            ps.executeUpdate();
            try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new Exception("Failed to insert question.");
    }

    private int insertChoiceTx(java.sql.Connection cnx, int questionId, String text) throws Exception {
        String sql = "INSERT INTO choice_qcm(id_question, choice_text, is_correct) VALUES (?,?,0)";
        try (java.sql.PreparedStatement ps = cnx.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, questionId);
            ps.setString(2, text);
            ps.executeUpdate();
            try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to insert choice.");
    }

    private void setCorrectChoiceTx(java.sql.Connection cnx, int questionId, int choiceId) throws Exception {
        try (java.sql.PreparedStatement ps1 = cnx.prepareStatement("UPDATE choice_qcm SET is_correct=0 WHERE id_question=?")) {
            ps1.setInt(1, questionId);
            ps1.executeUpdate();
        }
        try (java.sql.PreparedStatement ps2 = cnx.prepareStatement("UPDATE choice_qcm SET is_correct=1 WHERE id_question=? AND id_choice=?")) {
            ps2.setInt(1, questionId);
            ps2.setInt(2, choiceId);
            int updated = ps2.executeUpdate();
            if (updated != 1) throw new Exception("Correct choice update failed.");
        }
    }
    private void insertAiQuizIntoDb(int ignored, String quizJson) throws Exception {
        // jobOfferId is already captured in the outer class field

        var gson = new com.google.gson.Gson();
        dto.AiQuizDTO dto = gson.fromJson(quizJson, dto.AiQuizDTO.class);

        if (dto == null || dto.questions == null || dto.questions.isEmpty())
            throw new Exception("AI returned empty quiz.");

        java.sql.Connection cnx = Utils.Mydb.getInstance().getConnection();
        cnx.setAutoCommit(false);

        try {
            for (dto.AiQuizDTO.AiQuestionDTO qdto : dto.questions) {

                // ---- validate question ----
                if (qdto == null || qdto.statement == null || qdto.statement.trim().isEmpty())
                    throw new Exception("Invalid question statement.");

                if (qdto.choices == null || qdto.choices.size() < 2)
                    throw new Exception("Each question must have at least 2 choices.");

                int correctCount = 0;
                for (var cdto : qdto.choices) {
                    if (cdto != null && Boolean.TRUE.equals(cdto.is_correct)) correctCount++;
                }
                if (correctCount != 1)
                    throw new Exception("Each question must have exactly 1 correct choice.");

                java.math.BigDecimal points = new java.math.BigDecimal(
                        (qdto.points == null ? 1.0 : qdto.points)
                ).setScale(2, java.math.RoundingMode.HALF_UP);

                // ---- insert question ----
                // use your existing questionService insert OR inline SQL (but must use same cnx to keep transaction)
                long questionId = insertQuestionTx(cnx, qdto.statement.trim(), points, jobOfferId);

                // ---- insert choices ----
                Integer correctChoiceId = null;
                for (var cdto : qdto.choices) {
                    String text = (cdto == null || cdto.text == null) ? "" : cdto.text.trim();
                    if (text.isEmpty()) throw new Exception("Choice text empty.");

                    int choiceId = insertChoiceTx(cnx, (int)questionId, text); // inserts is_correct=0
                    if (Boolean.TRUE.equals(cdto.is_correct)) correctChoiceId = choiceId;
                }

                // ---- set correct choice (your existing logic) ----
                if (correctChoiceId == null) throw new Exception("No correct choice detected.");
                setCorrectChoiceTx(cnx, (int)questionId, correctChoiceId);
            }

            cnx.commit();
        } catch (Exception e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }
    private void updatequestion() {
        question q = view.questionTable.getSelectionModel().getSelectedItem();
        if (q == null) { showInfo("Select question", "Select a question to update."); return; }

        String statement = safe(view.statementArea.getText());
        if (statement.isEmpty()) { showInfo("Statement required", "Write the question."); return; }

        BigDecimal points = parsePoints(view.pointsField.getText());

        try {
            questionService.updatequestion(q.getIdQuestion(), statement, points);
            loadQuestionsByJob();
            selectquestionById(q.getIdQuestion());
            view.lblStatus.setText("Updated question #" + q.getIdQuestion());
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void deletequestion() {
        question q = view.questionTable.getSelectionModel().getSelectedItem();
        if (q == null) { showInfo("Select question", "Select a question to delete."); return; }

        try {
            questionService.deletequestion(q.getIdQuestion());
            loadQuestionsByJob();
            view.choiceTable.getItems().clear();
            view.choiceTextField.clear();
            clearquestionForm();
            view.lblStatus.setText("Deleted question #" + q.getIdQuestion());
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void clearquestionForm() {
        view.statementArea.clear();
        view.pointsField.clear();
        view.questionTable.getSelectionModel().clearSelection();
    }

    private void selectquestionById(int idquestion) {
        for (question q : view.questionTable.getItems()) {
            if (q.getIdQuestion() == idquestion) {
                view.questionTable.getSelectionModel().select(q);
                view.questionTable.scrollTo(q);
                break;
            }
        }
    }

    // ---------- Choice CRUD (kahoot style) ----------
    private void addChoice() {
        question q = view.questionTable.getSelectionModel().getSelectedItem();
        if (q == null) { showInfo("Select question", "Select a question first."); return; }

        String text = safe(view.choiceTextField.getText());
        if (text.isEmpty()) { showInfo("Answer required", "Write an answer."); return; }

        try {
            int newId = choiceqcmService.addChoice(q.getIdQuestion(), text); // always is_correct=0
            loadChoicesForquestion(q.getIdQuestion());
            selectChoiceById(newId);
            view.choiceTextField.clear();
            view.lblStatus.setText("Added answer #" + newId);
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void updateChoiceText() {
        choiceqcm c = view.choiceTable.getSelectionModel().getSelectedItem();
        if (c == null) { showInfo("Select answer", "Select an answer to edit."); return; }

        String text = safe(view.choiceTextField.getText());
        if (text.isEmpty()) { showInfo("Answer required", "Write an answer."); return; }

        try {
            choiceqcmService.updateChoiceText(c.getIdChoice(), text);
            loadChoicesForquestion(c.getIdQuestion());
            selectChoiceById(c.getIdChoice());
            view.lblStatus.setText("Edited answer #" + c.getIdChoice());
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void deleteChoice() {
        choiceqcm c = view.choiceTable.getSelectionModel().getSelectedItem();
        if (c == null) { showInfo("Select answer", "Select an answer to remove."); return; }

        try {
            int qId = c.getIdQuestion();
            choiceqcmService.deleteChoice(c.getIdChoice());
            loadChoicesForquestion(qId);
            view.choiceTextField.clear();
            view.lblStatus.setText("Removed answer #" + c.getIdChoice());
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void setCorrectChoice() {
        question q = view.questionTable.getSelectionModel().getSelectedItem();
        choiceqcm c = view.choiceTable.getSelectionModel().getSelectedItem();
        if (q == null || c == null) { showInfo("Select", "Select a question and an answer."); return; }

        try {
            choiceqcmService.setCorrectChoice(q.getIdQuestion(), c.getIdChoice());
            loadChoicesForquestion(q.getIdQuestion());
            view.lblStatus.setText("Correct set ✔");
        } catch (SQLException ex) {
            showError("DB Error", ex.getMessage());
        }
    }

    private void selectChoiceById(int idChoice) {
        for (choiceqcm c : view.choiceTable.getItems()) {
            if (c.getIdChoice() == idChoice) {
                view.choiceTable.getSelectionModel().select(c);
                view.choiceTable.scrollTo(c);
                break;
            }
        }
    }

    // ---------- utils ----------
    private String safe(String s) { return s == null ? "" : s.trim(); }

    private BigDecimal parsePoints(String txt) {
        String t = safe(txt);
        if (t.isEmpty()) return new BigDecimal("1.00");
        try { return new BigDecimal(t); }
        catch (Exception e) { return new BigDecimal("1.00"); }
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
    private int extractInt(String s, String regex, int def) {
        if (s == null) return def;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(s);
        return m.find() ? Integer.parseInt(m.group(1)) : def;
    }

    private void validateAi(dto.AiQuizDTO dtoObj, int nQ, int nC) throws Exception {
        if (dtoObj == null || dtoObj.questions == null) throw new Exception("Empty quiz.");
        if (dtoObj.questions.size() != nQ)
            throw new Exception("Wrong question count: got " + dtoObj.questions.size() + " expected " + nQ);

        for (int i = 0; i < dtoObj.questions.size(); i++) {
            var q = dtoObj.questions.get(i);
            if (q == null) throw new Exception("Null question at index " + i);

            if (q.choices == null || q.choices.size() != nC)
                throw new Exception("Wrong choices count at Q" + (i+1) + ": got " +
                        (q.choices == null ? 0 : q.choices.size()) + " expected " + nC);

            int correct = 0;
            for (var c : q.choices) if (c != null && Boolean.TRUE.equals(c.is_correct)) correct++;
            if (correct != 1)
                throw new Exception("Q" + (i+1) + " must have exactly 1 correct choice, got " + correct);
        }
    }
}