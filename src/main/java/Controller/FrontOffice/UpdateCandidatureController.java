package Controller.FrontOffice;

import Service.CandidatureService;
import Model.Candidature;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateCandidatureController {

    @FXML
    private TextField salaryField;

    @FXML
    private TextArea coverLetterArea;

    @FXML
    private TextField cvPathField;

    private Candidature candidature;
    private CandidatureService candidatureService;

    public void initialize() {
        candidatureService = new CandidatureService();
    }

    // VERY IMPORTANT
    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;

        salaryField.setText(String.valueOf(candidature.getExpectedSalary()));
        coverLetterArea.setText(candidature.getCoverLetter());
        cvPathField.setText(candidature.getCvPath());
    }

    @FXML
    private void handleSave() {
        try {
            candidature.setExpectedSalary(Double.parseDouble(salaryField.getText()));
            candidature.setCoverLetter(coverLetterArea.getText());
            candidature.setCvPath(cvPathField.getText());

            candidatureService.updateCandidature(candidature);

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) salaryField.getScene().getWindow();
        stage.close();
    }
}