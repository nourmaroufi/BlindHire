package ui;

import Model.JobOffer;
import Model.score;
import Service.JobOfferService;
import Service.InterviewService;
import Service.userservice;
import Service.scoreService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import Model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.sql.SQLException;
import java.util.List;

public class InterviewPage {

    private static final String PAGE_BG       = "#0f172a";
    private static final String CARD_BG       = "rgba(255,255,255,0.05)";
    private static final String ACCENT_INDIGO = "#6366f1";
    private static final String ACCENT_CYAN   = "#06b6d4";
    private static final String ACCENT_GREEN  = "#10b981";
    private static final String ACCENT_AMBER  = "#f59e0b";

    private final Parent root;
    private final JobOfferService jobOfferService = new JobOfferService();
    private final userservice userService = new userservice();
    private final scoreService    scoreService    = new scoreService();
    private final InterviewService interviewService = new InterviewService();

    public InterviewPage() {
        root = buildView();
    }

    public Parent getRoot() { return root; }

    // ─────────────────────────────────────────────────────────────────────────

    private Parent buildView() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(36, 40, 40, 40));
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        // ── Header ────────────────────────────────────────────────────────────
        Label titleLbl = new Label("📅  Interviews");
        titleLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subLbl = new Label("Click a job offer to see candidates who passed the quiz");
        subLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.40);");
        page.getChildren().add(new VBox(4, titleLbl, subLbl));

        // ── Job offer rows ────────────────────────────────────────────────────
        // Get current recruiter's id
        int recruiterId = 0;
        Model.User currentUser = userService.getCurrentUser();
        if (currentUser != null) recruiterId = currentUser.getId();

        List<JobOffer> jobs;
        try {
            jobs = jobOfferService.getJobOffersByRecruiterId(recruiterId);
        } catch (Exception e) {
            page.getChildren().add(errorLabel("Failed to load job offers: " + e.getMessage()));
            return wrap(page);
        }

        if (jobs.isEmpty()) {
            page.getChildren().add(errorLabel("No job offers found."));
            return wrap(page);
        }

        // Only show jobs that have at least one candidate who passed a quiz
        boolean anyVisible = false;
        for (JobOffer job : jobs) {
            try {
                if (scoreService.countAcceptedByJob(job.getId()) > 0) {
                    page.getChildren().add(buildJobRow(job));
                    anyVisible = true;
                }
            } catch (Exception ignored) {}
        }

        if (!anyVisible) {
            page.getChildren().add(errorLabel("No candidates have passed a quiz for any of your job offers yet."));
        }

        return wrap(page);
    }

    // ── One collapsible job offer row ─────────────────────────────────────────

    private VBox buildJobRow(JobOffer job) {
        // Count accepted candidates for badge
        int passedCount = 0;
        try { passedCount = scoreService.countAcceptedByJob(job.getId()); }
        catch (Exception ignored) {}

        VBox block = new VBox(0);
        block.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: rgba(255,255,255,0.09);" +
                        "-fx-border-width: 1; -fx-border-radius: 16;"
        );

        // ── Header row ────────────────────────────────────────────────────────
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-cursor: hand; -fx-background-radius: 16;");

        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(42, 42); iconCircle.setMinSize(42, 42);
        iconCircle.setStyle("-fx-background-color: rgba(99,102,241,0.20); -fx-background-radius: 50;");
        Label iconLbl = new Label("💼"); iconLbl.setStyle("-fx-font-size: 18px;");
        iconCircle.getChildren().add(iconLbl);

        VBox jobInfo = new VBox(4);
        HBox.setHgrow(jobInfo, Priority.ALWAYS);

        Label jobTitleLbl = new Label(job.getTitle());
        jobTitleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        final int pc = passedCount;
        Label countBadge = new Label(pc + " passed candidate" + (pc != 1 ? "s" : ""));
        countBadge.setStyle(pc > 0
                ? "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: " + ACCENT_GREEN + ";" +
                "-fx-padding: 2 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"
                : "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: rgba(255,255,255,0.30);" +
                "-fx-padding: 2 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"
        );
        jobInfo.getChildren().addAll(jobTitleLbl, countBadge);

        Label chevron = new Label("▶");
        chevron.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 11px;");

        header.getChildren().addAll(iconCircle, jobInfo, chevron);

        // ── Drop panel ────────────────────────────────────────────────────────
        VBox dropPanel = new VBox(12);
        dropPanel.setPadding(new Insets(4, 20, 18, 20));
        dropPanel.setVisible(false);
        dropPanel.setManaged(false);
        dropPanel.setStyle("-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1 0 0 0;");

        final boolean[] loaded = {false};

        header.setOnMouseClicked(e -> {
            boolean open = dropPanel.isVisible();
            dropPanel.setVisible(!open);
            dropPanel.setManaged(!open);
            chevron.setText(open ? "▶" : "▼");
            chevron.setStyle(!open
                    ? "-fx-text-fill: " + ACCENT_CYAN + "; -fx-font-size: 11px;"
                    : "-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 11px;");
            header.setStyle(!open
                    ? "-fx-cursor: hand; -fx-background-color: rgba(99,102,241,0.08); -fx-background-radius: 16 16 0 0;"
                    : "-fx-cursor: hand; -fx-background-radius: 16;");

            // Lazy load on first open
            if (!open && !loaded[0]) {
                loaded[0] = true;
                dropPanel.getChildren().clear();
                try {
                    List<score> scores = scoreService.getAcceptedByJob(job.getId());
                    if (scores.isEmpty()) {
                        Label none = new Label("No candidates have passed the quiz for this job yet.");
                        none.setStyle("-fx-text-fill: rgba(255,255,255,0.30); -fx-font-size: 13px; -fx-padding: 10 0 4 0;");
                        dropPanel.getChildren().add(none);
                    } else {
                        FlowPane flow = new FlowPane(16, 14);
                        flow.setPadding(new Insets(12, 0, 4, 0));
                        for (score s : scores) {
                            flow.getChildren().add(buildScoreCard(s, job.getTitle(), job.getId()));
                        }
                        dropPanel.getChildren().add(flow);
                    }
                } catch (Exception ex) {
                    dropPanel.getChildren().add(errorLabel("Error loading candidates: " + ex.getMessage()));
                }
            }
        });

        header.setOnMouseEntered(ev -> { if (!dropPanel.isVisible()) header.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 16;"); });
        header.setOnMouseExited(ev  -> { if (!dropPanel.isVisible()) header.setStyle("-fx-cursor: hand; -fx-background-radius: 16;"); });

        block.getChildren().addAll(header, dropPanel);
        return block;
    }

    // ── Score card ────────────────────────────────────────────────────────────

    private VBox buildScoreCard(score s, String jobTitle, int jobId) {
        VBox card = new VBox(14);
        card.setPrefWidth(240);
        card.setPadding(new Insets(18, 20, 18, 20));

        String styleNormal = "-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 16;" +
                "-fx-border-color: rgba(255,255,255,0.10); -fx-border-width: 1; -fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 10, 0, 0, 3);";
        String styleHover  = "-fx-background-color: rgba(99,102,241,0.12); -fx-background-radius: 16;" +
                "-fx-border-color: " + ACCENT_INDIGO + "; -fx-border-width: 1.5; -fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.22), 14, 0, 0, 5);";
        card.setStyle(styleNormal);
        card.setOnMouseEntered(e -> card.setStyle(styleHover));
        card.setOnMouseExited(e  -> card.setStyle(styleNormal));

        // Avatar + info
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(40, 40); avatar.setMinSize(40, 40);
        avatar.setStyle("-fx-background-color: linear-gradient(to bottom right, " +
                ACCENT_INDIGO + ", " + ACCENT_CYAN + "); -fx-background-radius: 50;");
        String displayName = s.getCandidateUsername() != null ? s.getCandidateUsername() : "Candidate #" + s.getIdUser();
        // Avatar initials: first letter of each word, max 2 chars
        String[] parts = displayName.split("\\s+");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
                : displayName.substring(0, Math.min(2, displayName.length()));
        Label avLbl = new Label(initials.toUpperCase());
        avLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        avatar.getChildren().add(avLbl);

        VBox infoBox = new VBox(3);
        Label nameLbl = new Label(displayName);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        String dateStr = s.getCreatedAt() != null
                ? s.getCreatedAt().toLocalDateTime().toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "—";
        Label dateLbl = new Label("Passed on " + dateStr);
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.35);");
        infoBox.getChildren().addAll(nameLbl, dateLbl);
        topRow.getChildren().addAll(avatar, infoBox);

        // Score value
        Label scoreVal = new Label(String.format("%.0f%%", s.getScore().doubleValue()));
        scoreVal.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_GREEN + ";");
        Label scoreSub = new Label("quiz score");
        scoreSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.30);");

        // Accepted badge
        Label badge = new Label("✓  Accepted");
        badge.setStyle("-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: " + ACCENT_GREEN + ";" +
                "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Divider
        Region div = new Region();
        div.setPrefHeight(1); div.setMaxWidth(Double.MAX_VALUE);
        div.setStyle("-fx-background-color: rgba(255,255,255,0.07);");

        // Check if interview already scheduled for this score
        boolean alreadyScheduled = false;
        try { alreadyScheduled = interviewService.existsByScore(s.getIdScore()); }
        catch (Exception ignored) {}

        Button schedBtn;
        if (alreadyScheduled) {
            // ── "View Schedule" variant ───────────────────────────────────────
            String viewN = "-fx-background-color: rgba(16,185,129,0.15);" +
                    "-fx-text-fill: " + ACCENT_GREEN + "; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-padding: 9 0; -fx-background-radius: 25; -fx-cursor: hand;" +
                    "-fx-border-color: " + ACCENT_GREEN + "; -fx-border-radius: 25; -fx-border-width: 1;";
            String viewH = "-fx-background-color: rgba(16,185,129,0.28);" +
                    "-fx-text-fill: " + ACCENT_GREEN + "; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-padding: 9 0; -fx-background-radius: 25; -fx-cursor: hand;" +
                    "-fx-border-color: " + ACCENT_GREEN + "; -fx-border-radius: 25; -fx-border-width: 1.5;";
            schedBtn = new Button("✅  View Schedule");
            schedBtn.setMaxWidth(Double.MAX_VALUE);
            schedBtn.setStyle(viewN);
            schedBtn.setOnMouseEntered(e -> schedBtn.setStyle(viewH));
            schedBtn.setOnMouseExited(e  -> schedBtn.setStyle(viewN));
            schedBtn.setOnAction(e -> {
                // Placeholder — replace with: BlindHireApp.loadScene(new ViewInterviewDetails(s.getIdScore()).getRoot(), ...)
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Interview Already Scheduled");
                alert.setHeaderText(null);
                alert.setContentText("An interview has already been scheduled for " + displayName + ".");
                alert.showAndWait();
            });
        } else {
            // ── "Schedule Interview" variant ──────────────────────────────────
            String btnN = "-fx-background-color: linear-gradient(to right, " + ACCENT_INDIGO + ", #818cf8);" +
                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-padding: 9 0; -fx-background-radius: 25; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.30), 8, 0, 0, 3);";
            String btnH = "-fx-background-color: linear-gradient(to right, #4f46e5, " + ACCENT_INDIGO + ");" +
                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-padding: 9 0; -fx-background-radius: 25; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.50), 12, 0, 0, 5);";
            schedBtn = new Button("📅  Schedule Interview");
            schedBtn.setMaxWidth(Double.MAX_VALUE);
            schedBtn.setStyle(btnN);
            schedBtn.setOnMouseEntered(e -> schedBtn.setStyle(btnH));
            schedBtn.setOnMouseExited(e  -> schedBtn.setStyle(btnN));
            schedBtn.setOnAction(e -> openAddInterview(s.getIdScore(), s.getIdUser(), displayName, jobTitle, jobId));
        }

        card.getChildren().addAll(topRow, scoreVal, scoreSub, badge, div, schedBtn);
        return card;
    }

    // ── Open add interview form ───────────────────────────────────────────────

    private void openAddInterview(int idScore, int candidateId, String candidateUsername, String jobTitle, int jobId) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/addInterview.fxml"));
            javafx.scene.Parent form = loader.load();
            Controller.AddInterviewController ctrl = loader.getController();
            ctrl.prefillFromScore((long) idScore, (long) candidateId, candidateUsername, jobTitle);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Schedule Interview — " + jobTitle);
            stage.setScene(new javafx.scene.Scene(form, 700, 580));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Could not open interview form: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Parent wrap(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-border-width: 0; -fx-background: " + PAGE_BG + ";");
        return sp;
    }

    private Label errorLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 13px; -fx-padding: 10 0;");
        return l;
    }
}