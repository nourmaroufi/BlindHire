package Controller.FrontOffice;

import Model.Candidature;
import Model.JobOffer;
import Model.Candidat;
import Service.CandidatureService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

public class AddCandidatureController {

    @FXML private TextArea coverLetterField;
    @FXML private TextField cvField;
    @FXML private TextField portfolioField;
    @FXML private TextField salaryField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button submitButton;

    private File selectedCV;
    private JobOffer selectedJob;
    private Candidat currentCandidate;

    private final CandidatureService candidatureService = new CandidatureService();

    public void setJobAndCandidate(JobOffer job, Candidat candidate) {
        this.selectedJob = job;
        this.currentCandidate = candidate;
    }

    @FXML
    private void handleBrowseCV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = chooser.showOpenDialog(null);
        if (file != null) {
            selectedCV = file;
            cvField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmit() {
        if (selectedJob == null || currentCandidate == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Application context missing.");
            return;
        }

        if (!validateForm()) return;

        try {
            submitButton.setDisable(true);

            Candidature candidature = new Candidature();
            candidature.setCoverLetter(coverLetterField.getText());
            candidature.setCvPath(selectedCV.getAbsolutePath());
            candidature.setPortfolioUrl(portfolioField.getText());

            if (!salaryField.getText().isEmpty()) {
                candidature.setExpectedSalary(Double.parseDouble(salaryField.getText()));
            }

            candidature.setJobOfferId(selectedJob.getId());
            candidature.setCandidateId(currentCandidate.getId_c());
            candidature.setApplicationDate(LocalDate.now());
            candidature.setStatus("pending");

            candidatureService.addCandidature(candidature);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Application submitted successfully!");
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit application: " + e.getMessage());
        } finally {
            submitButton.setDisable(false);
        }
    }

    private boolean validateForm() {
        if (coverLetterField.getText().isEmpty()) {
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

        if (!salaryField.getText().isEmpty()) {
            try {
                Double.parseDouble(salaryField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Salary must be a number.");
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        coverLetterField.clear();
        cvField.clear();
        portfolioField.clear();
        salaryField.clear();
        termsCheckBox.setSelected(false);
        selectedCV = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}