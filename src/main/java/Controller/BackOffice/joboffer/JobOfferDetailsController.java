package Controller.BackOffice.joboffer;
import java.net.URL;
import java.io.IOException;

import Controller.BackOffice.Candidature.CandidatureController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import Model.JobOffer;
import Model.Role;
import Model.User;
import Service.userservice;
import Service.JobOfferService;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class JobOfferDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label recruiterLabel;
    @FXML private Label dateLabel;
    @FXML private Label typeLabel;
    @FXML private Label statusBadge;
    @FXML private TextArea descriptionArea;
    @FXML private Label salaryLabel;
    @FXML private FlowPane skillsContainer;
    @FXML private Button deleteButton;
    @FXML private Button modifyButton;
    @FXML private Button editQuizButton;
    @FXML private Button viewApplicationsButton;


    private JobOffer jobOffer;

    // ── Passed in from JobOfferListController before showing this popup ──────
    private User currentUser;
    private JobOfferListController.PageMode pageMode = JobOfferListController.PageMode.MY_JOBS;

    /** Set BEFORE calling setJobOffer() so displayJobDetails() can apply button visibility. */
    public void setContext(User user, JobOfferListController.PageMode mode) {
        this.currentUser = user;
        this.pageMode    = mode;
    }

    public void setJobOffer(JobOffer offer) {
        this.jobOffer = offer;
        displayJobDetails();
    }
    @FXML
    private void handleEditQuiz() {
        if (jobOffer == null) return;
        ui.QuestionCrudPanel quizPanel = new ui.QuestionCrudPanel(
                jobOffer.getId(),
                jobOffer.getTitle(),
                jobOffer.getRequiredSkills()
        );
        javafx.scene.Scene scene = new javafx.scene.Scene(quizPanel, 920, 660);
        javafx.stage.Stage quizStage = new javafx.stage.Stage();
        quizStage.setTitle("Edit Quiz — " + jobOffer.getTitle());
        quizStage.setScene(scene);
        quizStage.setMinWidth(780);
        quizStage.setMinHeight(540);
        quizStage.setResizable(true);
        quizStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        quizStage.initOwner(titleLabel.getScene().getWindow());
        quizStage.show();
    }

    @FXML
    private void handleViewApplications() {
        if (jobOffer == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/BackOffice/candidature.fxml")
            );
            Parent root = loader.load();

            CandidatureController controller = loader.getController();

            // 🔥 pass the job offer id
            controller.setJobOfferFilter(jobOffer.getId());

            Stage stage = new Stage();
            stage.setTitle("Applications - " + jobOffer.getTitle());
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            // close details window
            Stage currentStage = (Stage) titleLabel.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open applications page.");
        }
    }

    private void displayJobDetails() {
        if (jobOffer == null) return;

        titleLabel.setText(jobOffer.getTitle());
        recruiterLabel.setText(getRecruiterName(jobOffer.getRecruiterId()));
        typeLabel.setText(jobOffer.getType() != null ? jobOffer.getType() : "Full-time");
        descriptionArea.setText(jobOffer.getDescription());

        String status = jobOffer.getStatus() != null ? jobOffer.getStatus() : "Active";
        statusBadge.setText(status);
        String statusColor = switch (status.toLowerCase()) {
            case "active" -> "#10b981";
            case "pending" -> "#f59e0b";
            case "closed" -> "#ef4444";
            default -> "#6b7280";
        };
        statusBadge.setStyle("-fx-background-color: " + statusColor + "15; -fx-text-fill: " + statusColor +
                "; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Set date
        if (jobOffer.getPostingDate() != null) {
            LocalDate postingDate = jobOffer.getPostingDate();
            long daysAgo = ChronoUnit.DAYS.between(postingDate, LocalDate.now());

            if (daysAgo == 0) {
                dateLabel.setText("Today");
            } else if (daysAgo == 1) {
                dateLabel.setText("Yesterday");
            } else {
                dateLabel.setText(daysAgo + " days ago");
            }
        } else {
            dateLabel.setText("Recently");
        }

        // Display salary
        if (salaryLabel != null) {
            if (jobOffer.getOfferedSalary() != null && jobOffer.getOfferedSalary() > 0) {
                salaryLabel.setText(String.format("💰 %.0f TND / month (approx.)", jobOffer.getOfferedSalary()));
            } else {
                salaryLabel.setText("💰 Salary: Competitive");
            }
        }

        // ── Button visibility: same rules as job cards ───────────────────────────
        // MY_JOBS  → all buttons shown (admin + recruteur, it's their own job)
        // ALL_JOBS → buttons shown for admin only; hidden for recruteur
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.admin;
        boolean showActions = (pageMode == JobOfferListController.PageMode.MY_JOBS) || isAdmin;

        if (deleteButton       != null) deleteButton.setVisible(showActions);
        if (deleteButton       != null) deleteButton.setManaged(showActions);
        if (modifyButton       != null) modifyButton.setVisible(showActions);
        if (modifyButton       != null) modifyButton.setManaged(showActions);
        if (editQuizButton        != null) editQuizButton.setVisible(showActions);
        if (editQuizButton        != null) editQuizButton.setManaged(showActions);
        if (viewApplicationsButton != null) viewApplicationsButton.setVisible(showActions);
        if (viewApplicationsButton != null) viewApplicationsButton.setManaged(showActions);

        // Display skills if available
        if (jobOffer.getRequiredSkills() != null && !jobOffer.getRequiredSkills().isEmpty()) {
            String[] skills = jobOffer.getRequiredSkills().split(",");
            for (String skill : skills) {
                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    Label skillTag = new Label(trimmedSkill);
                    skillTag.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 6 15; -fx-background-radius: 20; " +
                            "-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-weight: 500;");
                    skillsContainer.getChildren().add(skillTag);
                }
            }
        }
    }

    private String getRecruiterName(int recruiterId) {
        try {
            User recruiter = new userservice().getUserById(recruiterId);
            if (recruiter != null) return recruiter.getNom() + " " + recruiter.getPrenom();
        } catch (Exception ignored) {}
        return "Recruiter #" + recruiterId;
    }

    @FXML
    private void goToModify() {
        try {
            Stage currentStage = (Stage) titleLabel.getScene().getWindow();

            System.out.println("JavaFX version: " + System.getProperty("javafx.version"));
            System.out.println("Trying to load: /updatejoboffer.fxml");

            URL fxmlUrl = getClass().getResource("/BackOffice/updatejoboffer.fxml");
            System.out.println("FXML URL: " + fxmlUrl);

            if (fxmlUrl == null) {
                showAlert("Error", "FXML file not found! Make sure it's in the correct location.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            UpdateJobOfferController updateController = loader.getController();
            updateController.setJobOffer(jobOffer);

            Stage modifyStage = new Stage();
            modifyStage.setTitle("Modify Job Offer - " + jobOffer.getTitle());
            modifyStage.setScene(new Scene(root, 600, 700));
            modifyStage.centerOnScreen();
            modifyStage.show();

            currentStage.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Error", "Could not open modify window: " + ex.getMessage());
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDelete() {

        if (jobOffer == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Job Offer");
        confirm.setContentText("Are you sure you want to delete this job offer?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                JobOfferService service = new JobOfferService();
                service.deleteJobOffer(jobOffer.getId());

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Deleted");
                success.setHeaderText(null);
                success.setContentText("Job offer deleted successfully.");
                success.showAndWait();

                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.close();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Could not delete job offer: " + e.getMessage());
            }
        }
    }




    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}