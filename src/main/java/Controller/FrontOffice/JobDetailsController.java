package Controller.FrontOffice;

import Model.JobOffer;
import Model.User;
import Service.AiMatchingService;
import Service.CandidatureService;
import Service.FavJobService;
import Service.JobOfferService;
import Service.userservice;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import Utils.NavigationManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class JobDetailsController {

    @FXML private Label       matchPercentageLabel;
    @FXML private ProgressBar matchProgressBar;
    @FXML private Label       titleLabel;
    @FXML private Label       descriptionLabel;
    @FXML private Label       typeLabel;
    @FXML private Label       statusLabel;
    @FXML private Label       dateLabel;
    @FXML private Label       salaryLabel;
    @FXML private Circle      statusIndicator;
    @FXML private FlowPane    skillsContainer;
    @FXML private HBox        similarJobsContainer;
    @FXML private Button      backButton;
    @FXML private Button      applyButton;
    @FXML private Button      saveButton;

    private JobOffer        job;
    private JobOfferService jobService;
    private FavJobService   favService;
    private BorderPane      homeBorderPane;
    private User            currentUser;

    public void setHomeBorderPane(BorderPane borderPane) {
        this.homeBorderPane = borderPane;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        jobService = new JobOfferService();
        favService = new FavJobService();
        if (currentUser == null) {
            currentUser = new userservice().getCurrentUser();
        }
    }

    public void setJob(JobOffer job) {
        this.job = job;

        titleLabel.setText(job.getTitle());
        descriptionLabel.setText(job.getDescription());
        typeLabel.setText(formatJobType(job.getType()));

        String status = job.getStatus();
        statusLabel.setText(formatStatus(status));
        setStatusColor(status);

        if (job.getPostingDate() != null)
            dateLabel.setText(job.getPostingDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        else
            dateLabel.setText("Not specified");

        if (salaryLabel != null) {
            if (job.getOfferedSalary() != null && job.getOfferedSalary() > 0)
                salaryLabel.setText(String.format("%.0f TND/month (approx.)", job.getOfferedSalary()));
            else
                salaryLabel.setText("Competitive");
        }

        loadSkills(job.getRequiredSkills());
        loadSimilarJobs();
        syncSaveButton();
        calculateAiMatch();
    }

    // ─── AI MATCH ─────────────────────────────────────────────────────────────

    private void calculateAiMatch() {
        if (job == null) return;

        if (currentUser == null) {
            matchPercentageLabel.setText("Log in to see your match score");
            matchPercentageLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #94a3b8;");
            return;
        }

        String candidateSkills = currentUser.getSkills();
        String jobSkills       = job.getRequiredSkills();

        matchPercentageLabel.setText("Calculating match...");
        matchPercentageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        matchProgressBar.setProgress(-1);

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return AiMatchingService.getMatchScore(jobSkills, candidateSkills);
            }
        };

        task.setOnSucceeded(e -> {
            try {
                int pct = Integer.parseInt(task.getValue().trim());
                double progress = pct / 100.0;
                String color, emoji;
                if (pct >= 70)      { color = "#22c55e"; emoji = "Green"; }
                else if (pct >= 40) { color = "#f59e0b"; emoji = "Yellow"; }
                else                { color = "#ef4444"; emoji = "Red"; }
                matchPercentageLabel.setText(pct + "% Match");
                matchPercentageLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
                matchProgressBar.setProgress(progress);
                matchProgressBar.setStyle("-fx-accent: " + color + ";");
            } catch (NumberFormatException ex) {
                matchPercentageLabel.setText("Match calculated");
                matchPercentageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b;");
                matchProgressBar.setProgress(0);
            }
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            matchPercentageLabel.setText("Match score unavailable");
            matchPercentageLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ef4444;");
            matchProgressBar.setProgress(0);
            if (task.getException() != null) task.getException().printStackTrace();
        }));

        new Thread(task).start();
    }

    // ─── SAVE / FAV ───────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        if (job == null || currentUser == null || favService == null) return;
        try {
            boolean alreadySaved = favService.isSaved(currentUser.getId(), job.getId());
            if (alreadySaved) {
                favService.remove(currentUser.getId(), job.getId());
                setSaveButtonUnsaved();
                showToast("Removed from favourites", "#ef4444");
            } else {
                favService.save(currentUser.getId(), job.getId());
                setSaveButtonSaved();
                showToast("\"" + job.getTitle() + "\" saved successfully!", "#10b981");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showToast("Could not save — try again", "#ef4444");
        }
    }

    private void syncSaveButton() {
        if (saveButton == null || job == null || currentUser == null || favService == null) return;
        try {
            if (favService.isSaved(currentUser.getId(), job.getId()))
                setSaveButtonSaved();
            else
                setSaveButtonUnsaved();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void setSaveButtonSaved() {
        if (saveButton == null) return;
        saveButton.setText("Saved");
        saveButton.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white;" +
                        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 40;" +
                        "-fx-background-radius: 30; -fx-border-color: #10b981;" +
                        "-fx-border-width: 2; -fx-border-radius: 30; -fx-cursor: hand;"
        );
    }

    private void setSaveButtonUnsaved() {
        if (saveButton == null) return;
        saveButton.setText("Save for Later");
        saveButton.setStyle(
                "-fx-background-color: white; -fx-text-fill: #0fafdd;" +
                        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 40;" +
                        "-fx-background-radius: 30; -fx-border-color: #0fafdd;" +
                        "-fx-border-width: 2; -fx-border-radius: 30; -fx-cursor: hand;"
        );
    }

    private void showToast(String msg, String color) {
        if (saveButton == null) return;
        javafx.scene.Parent p = saveButton.getParent();
        while (p != null && !(p instanceof StackPane)) {
            p = p.getParent();
        }
        if (p == null) return;
        StackPane overlay = (StackPane) p;

        Label toast = new Label(msg);
        toast.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14 28;" +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);"
        );
        toast.setOpacity(0);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 70, 0));
        overlay.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(220), toast);
        slideUp.setFromY(18);
        slideUp.setToY(0);

        new ParallelTransition(fadeIn, slideUp).play();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(2200));
        fadeOut.setOnFinished(e -> overlay.getChildren().remove(toast));
        fadeOut.play();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private String formatJobType(String type) {
        if (type == null || type.isEmpty()) return "Not specified";
        switch (type.toLowerCase()) {
            case "full-time":  return "Full Time";
            case "part-time":  return "Part Time";
            case "contract":   return "Contract";
            case "internship": return "Internship";
            default:           return type;
        }
    }

    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "Unknown";
        switch (status.toLowerCase()) {
            case "open":    return "OPEN";
            case "closed":  return "CLOSED";
            case "pending": return "PENDING";
            default:        return status.toUpperCase();
        }
    }

    private void setStatusColor(String status) {
        if (status == null) { statusIndicator.setFill(Color.web("#94a3b8")); return; }
        switch (status.toLowerCase()) {
            case "open":
                statusIndicator.setFill(Color.web("#10b981"));
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
                break;
            case "closed":
                statusIndicator.setFill(Color.web("#ef4444"));
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                break;
            case "pending":
                statusIndicator.setFill(Color.web("#f59e0b"));
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
                break;
            default:
                statusIndicator.setFill(Color.web("#94a3b8"));
                statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        }
    }

    private void loadSkills(String skillsText) {
        skillsContainer.getChildren().clear();
        if (skillsText == null || skillsText.trim().isEmpty()) {
            Label l = new Label("No specific skills required");
            l.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 10;");
            skillsContainer.getChildren().add(l);
            return;
        }
        for (String skill : skillsText.split(",")) {
            String t = skill.trim();
            if (!t.isEmpty()) skillsContainer.getChildren().add(createSkillChip(t));
        }
    }

    private HBox createSkillChip(String skill) {
        HBox chip = new HBox();
        chip.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 8 15; -fx-background-radius: 20; -fx-border-color: #e2e8f0; -fx-border-radius: 20;");
        Label lbl = new Label(skill);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #0f172a; -fx-font-weight: 500;");
        chip.getChildren().add(lbl);
        chip.setOnMouseEntered(e -> {
            chip.setStyle("-fx-background-color: #0fafdd; -fx-padding: 8 15; -fx-background-radius: 20; -fx-border-color: #0fafdd; -fx-border-radius: 20;");
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: 500;");
        });
        chip.setOnMouseExited(e -> {
            chip.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 8 15; -fx-background-radius: 20; -fx-border-color: #e2e8f0; -fx-border-radius: 20;");
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #0f172a; -fx-font-weight: 500;");
        });
        return chip;
    }

    private void loadSimilarJobs() {
        similarJobsContainer.getChildren().clear();
        try {
            List<JobOffer> similar = jobService.getJobOffers().stream()
                    .filter(j -> j.getId() != job.getId())
                    .filter(j -> j.getType() != null && j.getType().equalsIgnoreCase(job.getType()))
                    .filter(j -> "Open".equalsIgnoreCase(j.getStatus()))
                    .limit(3).collect(Collectors.toList());
            if (similar.isEmpty()) {
                Label l = new Label("No similar jobs available");
                l.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
                similarJobsContainer.getChildren().add(l);
                return;
            }
            for (JobOffer sj : similar)
                similarJobsContainer.getChildren().add(createSimilarJobCard(sj));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createSimilarJobCard(JobOffer sj) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-cursor: hand;");
        Label title = new Label(sj.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        title.setWrapText(true);
        Label type = new Label(sj.getType());
        type.setStyle("-fx-font-size: 12px; -fx-text-fill: #0fafdd;");
        card.getChildren().addAll(title, type);
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e0f7fd; -fx-padding: 15; -fx-background-radius: 15; -fx-border-color: #0fafdd; -fx-border-width: 2; -fx-border-radius: 15; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> openJobDetails(sj));
        return card;
    }

    private void openJobDetails(JobOffer j) {
        if (homeBorderPane == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/jobDetails.fxml"));
            VBox root = loader.load();
            JobDetailsController ctrl = loader.getController();
            ctrl.setHomeBorderPane(homeBorderPane);
            ctrl.setCurrentUser(currentUser);
            ctrl.setJob(j);
            NavigationManager.navigateTo(homeBorderPane, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── FXML MOUSE HANDLERS ──────────────────────────────────────────────────

    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }

    @FXML
    private void handleApply() {
        if (job == null || homeBorderPane == null || currentUser == null) return;
        try {
            if (new CandidatureService().hasApplied(currentUser.getId(), job.getId())) {
                javafx.scene.control.Alert alert =
                        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Already Applied");
                alert.setHeaderText(null);
                alert.setContentText("You have already applied to " + job.getTitle());
                alert.showAndWait();
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/CandidatureForm.fxml"));
            ScrollPane form = loader.load();
            AddCandidatureController ctrl = loader.getController();
            ctrl.setJobAndUser(job, currentUser);
            ctrl.setHomeBorderPane(homeBorderPane);
            NavigationManager.navigateTo(homeBorderPane, form);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML private void handleBackHover() {
        if (backButton != null)
            backButton.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; -fx-background-radius: 20; -fx-cursor: hand; -fx-border-color: white; -fx-border-radius: 20;");
    }
    @FXML private void handleBackExit() {
        if (backButton != null)
            backButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; -fx-background-radius: 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 20;");
    }
    @FXML private void handleApplyHover() {
        if (applyButton != null)
            applyButton.setStyle("-fx-background-color: linear-gradient(to right, #0891b2, #057995); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 40; -fx-background-radius: 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,175,221,0.45), 15,0,0,8);");
    }
    @FXML private void handleApplyExit() {
        if (applyButton != null)
            applyButton.setStyle("-fx-background-color: linear-gradient(to right, #0fafdd, #057995); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 40; -fx-background-radius: 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,175,221,0.25), 15,0,0,5);");
    }
    @FXML private void handleSaveHover() {
        if (saveButton != null && !"Saved".equals(saveButton.getText()))
            saveButton.setStyle("-fx-background-color: #0fafdd; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 40; -fx-background-radius: 30; -fx-border-color: #0fafdd; -fx-border-width: 2; -fx-border-radius: 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,175,221,0.30), 10,0,0,4);");
    }
    @FXML private void handleSaveExit() {
        if (saveButton != null && !"Saved".equals(saveButton.getText()))
            saveButton.setStyle("-fx-background-color: white; -fx-text-fill: #0fafdd; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 40; -fx-background-radius: 30; -fx-border-color: #0fafdd; -fx-border-width: 2; -fx-border-radius: 30; -fx-cursor: hand;");
    }
}