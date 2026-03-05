package Controller.FrontOffice;

import Model.Candidature;
import Model.JobOffer;
import Model.User;
import Service.CandidatureService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import Utils.NavigationManager;

/**
 * AddCandidatureController
 *
 * User is passed directly via setJobAndUser() — no service call, no static.
 * Same pattern as ProfilePage: caller holds the User and passes it in.
 *
 *   jobController.openApplicationForm(job):
 *     ctrl.setJobAndUser(job, currentUser);   // currentUser came from ProfilePage
 *
 * candidature.candidate_id = currentUser.getId()  →  user.id (FK)
 */
public class AddCandidatureController {

    @FXML private TextArea  coverLetterField;
    @FXML private TextField cvField;
    @FXML private TextField portfolioField;
    @FXML private TextField salaryField;
    @FXML private CheckBox  termsCheckBox;
    @FXML private Button    submitButton;

    private File     selectedCV;
    private JobOffer selectedJob;
    private User     currentUser;

    private final CandidatureService candidatureService = new CandidatureService();

    private BorderPane homeBorderPane;

    public void setHomeBorderPane(BorderPane borderPane) {
        this.homeBorderPane = borderPane;
    }

    /** Called by jobController right after loader.load(). Replaces setJobAndCandidate(). */
    public void setJobAndUser(JobOffer job, User user) {
        this.selectedJob = job;
        this.currentUser = user;
    }

    // ── BROWSE CV ─────────────────────────────────────────────────────────────

    @FXML
    private void handleBrowseCV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CV");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files",      "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents",  "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("All Files",       "*.*")
        );
        Stage owner = (Stage) cvField.getScene().getWindow();
        File file   = chooser.showOpenDialog(owner);
        if (file != null) {
            selectedCV = file;
            cvField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleLoadCvFromProfile() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "No user session found.");
            return;
        }
        try {
            // Fetch the most recent CV path from the user's previous applications
            java.util.List<Model.Candidature> past =
                    candidatureService.getCandidaturesByUserId(currentUser.getId());

            String profileCv = past.stream()
                    .filter(c -> c.getCvPath() != null && !c.getCvPath().isBlank())
                    .findFirst()  // already ordered by date DESC
                    .map(Model.Candidature::getCvPath)
                    .orElse(null);

            if (profileCv == null) {
                showAlert(Alert.AlertType.INFORMATION, "No CV Found",
                        "You haven't uploaded a CV in any previous application.\nPlease use 'Browse Files'.");
                return;
            }
            File cvFile = new File(profileCv);
            if (!cvFile.exists()) {
                showAlert(Alert.AlertType.WARNING, "File Not Found",
                        "Your previous CV no longer exists at:\n" + profileCv);
                return;
            }
            selectedCV = cvFile;
            cvField.setText(cvFile.getAbsolutePath());
            showAlert(Alert.AlertType.INFORMATION, "CV Loaded",
                    "Loaded CV from your last application: " + cvFile.getName());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load CV from profile.");
        }
    }

    // ── SUBMIT ────────────────────────────────────────────────────────────────

    @FXML
    private void handleSubmit() {
        if (selectedJob == null || currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Application context missing.");
            return;
        }

        // ✅ Safety-net duplicate check (also checked in jobController before opening form)
        try {
            if (candidatureService.hasApplied(currentUser.getId(), selectedJob.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Already Applied",
                        "You have already applied to " + selectedJob.getTitle() );
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!validateForm()) return;

        try {
            submitButton.setDisable(true);

            Candidature candidature = new Candidature();
            candidature.setCandidateId(currentUser.getId());  // ← user.id → candidate_id
            candidature.setJobOfferId(selectedJob.getId());
            candidature.setApplicationDate(LocalDate.now());
            candidature.setStatus("pending");
            candidature.setCoverLetter(coverLetterField.getText().trim());
            candidature.setCvPath(selectedCV.getAbsolutePath());
            candidature.setPortfolioUrl(portfolioField.getText().trim());

            if (!salaryField.getText().trim().isEmpty())
                candidature.setExpectedSalary(Double.parseDouble(salaryField.getText().trim()));

            candidatureService.addCandidature(candidature);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Application submitted successfully!");
            clearForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Salary must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to submit application: " + e.getMessage());
        } finally {
            submitButton.setDisable(false);
        }
    }

    // ── VALIDATION ────────────────────────────────────────────────────────────

    private boolean validateForm() {
        if (coverLetterField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Cover letter is required.");
            return false;
        }
        if (selectedCV == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please upload your CV.");
            return false;
        }
        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "You must accept the terms.");
            return false;
        }
        if (!salaryField.getText().trim().isEmpty()) {
            try { Double.parseDouble(salaryField.getText().trim()); }
            catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Salary must be a number.");
                return false;
            }
        }
        return true;
    }

    // ── NAVIGATION ───────────────────────────────────────────────────────────────

    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void clearForm() {
        coverLetterField.clear();
        cvField.clear();
        portfolioField.clear();
        salaryField.clear();
        termsCheckBox.setSelected(false);
        selectedCV = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}