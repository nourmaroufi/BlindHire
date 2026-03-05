package Controller.BackOffice.joboffer;

import Model.JobOffer;
import Model.User;
import Service.JobOfferService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import ui.QuestionCrudPanel;

import java.time.LocalDate;

public class addJobOfferController {

    // ── wired by the caller ───────────────────────────────────────────────────
    private JobOfferListController listController;
    private User currentUser;

    public void setListController(JobOfferListController c) { this.listController = c; }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (recruiterInfoLabel != null && user != null)
            recruiterInfoLabel.setText(user.getNom() + " " + user.getPrenom()
                    + "  (" + user.getRole() + ")");
    }

    // ── FXML step-indicator labels ────────────────────────────────────────────
    @FXML private Circle step1Circle;
    @FXML private Label  step1Num, step1Title, step1Sub;
    @FXML private Circle step2Circle;
    @FXML private Label  step2Num, step2Title, step2Sub;

    // ── FXML panes ────────────────────────────────────────────────────────────
    @FXML private VBox step1Pane;
    @FXML private VBox step2Pane;

    // ── FXML form fields ─────────────────────────────────────────────────────
    @FXML private TextField        titleField;
    @FXML private TextArea         descriptionField;
    @FXML private ComboBox<String> typeField;
    @FXML private TextField        salaryField;
    @FXML private TextField        skillsField;
    @FXML private Label            recruiterInfoLabel;

    private final JobOfferService service = new JobOfferService();

    @FXML
    public void initialize() {
        typeField.getItems().addAll(
                "Full-time", "Part-time", "Contract", "Remote", "Internship");
    }

    // ── STEP 1 → STEP 2 ──────────────────────────────────────────────────────

    @FXML
    private void handleAddJobOffer() {
        if (!validateInputs()) return;

        if (currentUser == null) {
            alert(Alert.AlertType.ERROR, "Session Error", "Not logged in.",
                    "Please log in before adding a job offer."); return;
        }
        if (currentUser.getRole() == null ||
                (currentUser.getRole() != Model.Role.admin &&
                        currentUser.getRole() != Model.Role.recruteur)) {
            alert(Alert.AlertType.ERROR, "Access Denied", "Insufficient role.",
                    "Only admins and recruiters can add job offers."); return;
        }

        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm"); confirm.setHeaderText("Add New Job Offer");
            confirm.setContentText("Save this job and proceed to quiz setup?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            JobOffer job = new JobOffer(
                    titleField.getText().trim(),
                    descriptionField.getText().trim(),
                    currentUser.getId(),
                    typeField.getValue(), "Pending", LocalDate.now());

            if (skillsField != null && !skillsField.getText().trim().isEmpty())
                job.setRequiredSkills(skillsField.getText().trim());

            if (salaryField != null && !salaryField.getText().trim().isEmpty()) {
                try { job.setOfferedSalary(Double.parseDouble(salaryField.getText().trim())); }
                catch (NumberFormatException ignored) {}
            }

            int newJobId = service.addJobOffer(job);
            if (listController != null) listController.refresh();

            String skills = (skillsField != null) ? skillsField.getText().trim() : "";
            transitionToStep2(newJobId, job.getTitle(), skills);

        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, "Error", "Failed to Add Job Offer",
                    "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Swaps Step 1 form for Step 2 quiz panel inside the SAME window.
     * Updates the step indicator to reflect completion.
     */
    private void transitionToStep2(int jobOfferId, String jobTitle, String requiredSkills) {
        // ── Update step indicator ──────────────────────────────────────────
        // Step 1 → completed (teal check)
        step1Circle.setStyle(
                "-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #26d0ce, #1a9a9a);");
        step1Num.setText("✓");
        step1Title.setStyle("-fx-text-fill: #26d0ce; -fx-font-weight: 800; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");
        step1Sub.setText("Saved ✓");
        step1Sub.setStyle("-fx-text-fill: #26d0ce; -fx-font-size: 10; -fx-font-family: 'Segoe UI';");

        // Step 2 → active (navy gradient)
        step2Circle.setStyle(
                "-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #1a2980, #26d0ce);");
        step2Num.setStyle("-fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");
        step2Title.setStyle("-fx-text-fill: #1a2980; -fx-font-weight: 800; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");
        step2Sub.setStyle("-fx-text-fill: #26d0ce; -fx-font-size: 10; -fx-font-family: 'Segoe UI';");
        step2Sub.setText("Add questions & answers");

        // ── Hide Step 1, show Step 2 ───────────────────────────────────────
        step1Pane.setVisible(false);
        step1Pane.setManaged(false);

        step2Pane.setVisible(true);
        step2Pane.setManaged(true);
        javafx.scene.layout.VBox.setVgrow(step2Pane, javafx.scene.layout.Priority.ALWAYS);

        // ── Build compact quiz panel and inject it ─────────────────────────
        QuestionCrudPanel panel = new QuestionCrudPanel(jobOfferId, jobTitle, requiredSkills);
        javafx.scene.layout.VBox.setVgrow(panel, javafx.scene.layout.Priority.ALWAYS);
        step2Pane.getChildren().setAll(panel);

        // ── Maximize the window for the quiz panel ───────────────────────
        Stage stage = (Stage) step2Pane.getScene().getWindow();
        stage.setMaximized(true);

        // Wire "Publish Job" button to close this window
        panel.btnFinish.setOnAction(e -> stage.close());
    }

    // ── CANCEL ────────────────────────────────────────────────────────────────

    @FXML
    private void handleCancel(ActionEvent event) {
        if (hasUnsavedChanges()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Cancel");
            confirm.setHeaderText("Unsaved Changes");
            confirm.setContentText("Discard changes and close?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
                close(event);
        } else {
            close(event);
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private boolean validateInputs() {
        StringBuilder err = new StringBuilder();
        if (titleField.getText() == null || titleField.getText().trim().isEmpty())
            err.append("- Job Title is required\n");
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty())
            err.append("- Description is required\n");
        if (typeField.getValue() == null)
            err.append("- Job Type is required\n");
        if (err.length() > 0) {
            alert(Alert.AlertType.ERROR, "Validation Error",
                    "Please correct the following errors:", err.toString());
            return false;
        }
        return true;
    }

    private boolean hasUnsavedChanges() {
        return (titleField.getText() != null && !titleField.getText().trim().isEmpty()) ||
                (descriptionField.getText() != null && !descriptionField.getText().trim().isEmpty()) ||
                (typeField.getValue() != null);
    }

    private void close(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void alert(Alert.AlertType type, String title, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(header); a.setContentText(content);
        a.showAndWait();
    }
}