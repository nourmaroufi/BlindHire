package Controller.FrontOffice;
import Controller.FrontOffice.jobController;
import Model.JobOffer;
import Service.JobOfferService;
import Service.candidatService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import Service.AiMatchingService;

public class JobDetailsController {
    @FXML private Label matchPercentageLabel;
    @FXML private ProgressBar matchProgressBar;

    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label typeLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Circle statusIndicator;
    @FXML private FlowPane skillsContainer;
    @FXML private HBox similarJobsContainer;
    @FXML private Button backButton;
    @FXML private Button applyButton;
    @FXML private Button saveButton;

    private JobOffer job;
    private JobOfferService jobService;

    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    @FXML
    public void initialize() {
        jobService = new JobOfferService();
    }

    public void setJob(JobOffer job) {
        this.job = job;
        titleLabel.setText(job.getTitle());
        descriptionLabel.setText(job.getDescription());
        typeLabel.setText(formatJobType(job.getType()));

        String status = job.getStatus();
        statusLabel.setText(formatStatus(status));
        setStatusColor(status);

        calculateAiMatch();
        if (job.getPostingDate() != null) {
            dateLabel.setText(job.getPostingDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } else {
            dateLabel.setText("Not specified");
        }

        loadSkills(job.getRequiredSkills());
        loadSimilarJobs();
    }



    private String formatJobType(String type) {
        if (type == null || type.isEmpty()) return "Not specified";

        switch (type.toLowerCase()) {
            case "full-time": return "Full Time";
            case "part-time": return "Part Time";
            case "contract": return "Contract";
            case "internship": return "Internship";
            default: return type;
        }
    }

    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "Unknown";

        switch (status.toLowerCase()) {
            case "open": return "● OPEN";
            case "closed": return "● CLOSED";
            case "pending": return "● PENDING";
            default: return "● " + status.toUpperCase();
        }
    }
    private void calculateAiMatch() {

        if (job == null) return;

        String candidateSkills =
                candidatService.getCurrentCandidate().getSkills();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return AiMatchingService.getMatchScore(
                        job.getRequiredSkills(),
                        candidateSkills
                );
            }
        };

        task.setOnSucceeded(e -> {

            String result = task.getValue();

            // Example expected result: "82"
            try {
                double percentage = Double.parseDouble(result.trim());
                double progress = percentage / 100.0;

                matchPercentageLabel.setText(percentage + "% Match");
                matchProgressBar.setProgress(progress);

            } catch (Exception ex) {
                matchPercentageLabel.setText("Match calculated");
            }
        });

        task.setOnFailed(e -> {
            matchPercentageLabel.setText("Match unavailable");
        });

        new Thread(task).start();
    }
    private void setStatusColor(String status) {
        if (status == null) {
            statusIndicator.setFill(Color.web("#94a3b8"));
            return;
        }

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
            Label noSkills = new Label("No specific skills required");
            noSkills.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 10;");
            skillsContainer.getChildren().add(noSkills);
            return;
        }

        String[] skills = skillsText.split(",");
        for (String skill : skills) {
            String trimmedSkill = skill.trim();
            if (!trimmedSkill.isEmpty()) {
                skillsContainer.getChildren().add(createSkillChip(trimmedSkill));
            }
        }
    }

    private HBox createSkillChip(String skill) {
        HBox chip = new HBox();
        chip.setStyle("""
                -fx-background-color: #f1f5f9;
                -fx-padding: 8 15;
                -fx-background-radius: 20;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 20;
                """);

        Label skillLabel = new Label(skill);
        skillLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-font-weight: 500;");

        chip.getChildren().add(skillLabel);

        chip.setOnMouseEntered(e -> {
            chip.setStyle("""
                    -fx-background-color: #4f46e5;
                    -fx-padding: 8 15;
                    -fx-background-radius: 20;
                    -fx-border-color: #4f46e5;
                    -fx-border-radius: 20;
                    """);
            skillLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: 500;");
        });

        chip.setOnMouseExited(e -> {
            chip.setStyle("""
                    -fx-background-color: #f1f5f9;
                    -fx-padding: 8 15;
                    -fx-background-radius: 20;
                    -fx-border-color: #e2e8f0;
                    -fx-border-radius: 20;
                    """);
            skillLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-font-weight: 500;");
        });

        return chip;
    }

    private void loadSimilarJobs() {
        similarJobsContainer.getChildren().clear();

        try {
            List<JobOffer> allJobs = jobService.getJobOffers();
            List<JobOffer> similar = allJobs.stream()
                    .filter(j -> j.getId() != job.getId())
                    .filter(j -> j.getType() != null && j.getType().equalsIgnoreCase(job.getType()))
                    .filter(j -> "Open".equalsIgnoreCase(j.getStatus()))
                    .limit(3)
                    .collect(Collectors.toList());

            if (similar.isEmpty()) {
                Label noSimilar = new Label("No similar jobs available");
                noSimilar.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
                similarJobsContainer.getChildren().add(noSimilar);
                return;
            }

            for (JobOffer similarJob : similar) {
                similarJobsContainer.getChildren().add(createSimilarJobCard(similarJob));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createSimilarJobCard(JobOffer job) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setStyle("""
                -fx-background-color: #f8fafc;
                -fx-padding: 15;
                -fx-background-radius: 15;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 15;
                -fx-cursor: hand;
                """);

        Label title = new Label(job.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        title.setWrapText(true);

        Label type = new Label(job.getType());
        type.setStyle("-fx-font-size: 12px; -fx-text-fill: #4f46e5;");
        card.getChildren().addAll(title, type);
        card.setOnMouseEntered(e -> {
            card.setStyle("""
                    -fx-background-color: #eef2ff;
                    -fx-padding: 15;
                    -fx-background-radius: 15;
                    -fx-border-color: #4f46e5;
                    -fx-border-width: 2;
                    -fx-border-radius: 15;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.2), 10,0,0,3);
                    """);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("""
                    -fx-background-color: #f8fafc;
                    -fx-padding: 15;
                    -fx-background-radius: 15;
                    -fx-border-color: #e2e8f0;
                    -fx-border-radius: 15;
                    -fx-cursor: hand;
                    """);
        });

        return card;
    }

    private void openJobDetails(JobOffer job) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/JobDetails.fxml"));
            VBox root = loader.load();

            JobDetailsController controller = loader.getController();
            controller.setJob(job);

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Job Details - " + job.getTitle());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (homeController != null) {
            homeController.handleJobs();
        }
    }


    @FXML
    private void handleApply() {
        if (job == null || homeController == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/CandidatureForm.fxml"));
            ScrollPane form = loader.load();

            AddCandidatureController formController = loader.getController();
            formController.setJobAndCandidate(job, candidatService.getCurrentCandidate());
            homeController.getContentArea().getChildren().setAll(form);

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Failed to open application form for job: " + job.getTitle());
        }
    }

    @FXML
    private void handleSave() {
        showSaveConfirmation();
    }


    private void showSaveConfirmation() {
        String originalText = saveButton.getText();
        saveButton.setText("✓ Saved!");
        saveButton.setStyle("""
                -fx-background-color: #10b981;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-padding: 15 40;
                -fx-background-radius: 30;
                -fx-border-color: #10b981;
                -fx-border-width: 2;
                -fx-border-radius: 30;
                -fx-cursor: hand;
                """);

        System.out.println("💾 Job saved to bookmarks: " + job.getTitle());

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    saveButton.setText(originalText);
                    saveButton.setStyle("""
                            -fx-background-color: white;
                            -fx-text-fill: #4f46e5;
                            -fx-font-size: 16px;
                            -fx-font-weight: bold;
                            -fx-padding: 15 40;
                            -fx-background-radius: 30;
                            -fx-border-color: #4f46e5;
                            -fx-border-width: 2;
                            -fx-border-radius: 30;
                            -fx-cursor: hand;
                            """);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void handleBackHover() {
        if (backButton != null) {
            backButton.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.3);
                    -fx-text-fill: white;
                    -fx-font-size: 20px;
                    -fx-font-weight: bold;
                    -fx-min-width: 40;
                    -fx-min-height: 40;
                    -fx-max-width: 40;
                    -fx-max-height: 40;
                    -fx-background-radius: 20;
                    -fx-cursor: hand;
                    -fx-border-color: white;
                    -fx-border-radius: 20;
                    """);
        }
    }

    @FXML
    private void handleBackExit() {
        if (backButton != null) {
            backButton.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.2);
                    -fx-text-fill: white;
                    -fx-font-size: 20px;
                    -fx-font-weight: bold;
                    -fx-min-width: 40;
                    -fx-min-height: 40;
                    -fx-max-width: 40;
                    -fx-max-height: 40;
                    -fx-background-radius: 20;
                    -fx-cursor: hand;
                    -fx-border-color: rgba(255,255,255,0.3);
                    -fx-border-radius: 20;
                    """);
        }
    }

    @FXML
    private void handleApplyHover() {
        if (applyButton != null && !applyButton.getText().contains("✓")) {
            applyButton.setStyle("""
                    -fx-background-color: linear-gradient(to right, #4338ca, #4f46e5);
                    -fx-text-fill: white;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-padding: 15 40;
                    -fx-background-radius: 30;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.4), 15,0,0,8);
                    """);
        }
    }

    @FXML
    private void handleApplyExit() {
        if (applyButton != null && !applyButton.getText().contains("✓")) {
            applyButton.setStyle("""
                    -fx-background-color: linear-gradient(to right, #4f46e5, #6366f1);
                    -fx-text-fill: white;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-padding: 15 40;
                    -fx-background-radius: 30;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 15,0,0,5);
                    """);
        }
    }

    @FXML
    private void handleSaveHover() {
        if (saveButton != null && !saveButton.getText().contains("✓")) {
            saveButton.setStyle("""
                    -fx-background-color: #4f46e5;
                    -fx-text-fill: white;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-padding: 15 40;
                    -fx-background-radius: 30;
                    -fx-border-color: #4f46e5;
                    -fx-border-width: 2;
                    -fx-border-radius: 30;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.2), 10,0,0,3);
                    """);
        }
    }

    @FXML
    private void handleSaveExit() {
        if (saveButton != null && !saveButton.getText().contains("✓")) {
            saveButton.setStyle("""
                    -fx-background-color: white;
                    -fx-text-fill: #4f46e5;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-padding: 15 40;
                    -fx-background-radius: 30;
                    -fx-border-color: #4f46e5;
                    -fx-border-width: 2;
                    -fx-border-radius: 30;
                    -fx-cursor: hand;
                    """);
        }
    }




}