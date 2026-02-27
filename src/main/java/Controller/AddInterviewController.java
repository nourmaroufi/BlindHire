package Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Candidat;
import model.Interview;
import services.CandidatService;
import services.InterviewService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddInterviewController {

    @FXML
    private ComboBox<Candidat> candidateComboBox;

    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> timeComboBox;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextField jobOfferField;
    @FXML
    private TextField interviewerField;

    private InterviewService interviewService = new InterviewService();
    private CandidatService candidatService = new CandidatService();
    private void loadCandidates() {
        try {
            candidateComboBox.getItems().addAll(candidatService.afficherAll());
        } catch (SQLException e) {
            showAlert("Error", "Failed to load candidates: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Online", "In Person"));

        // Populate timeComboBox with half-hour slots
        timeComboBox.setItems(FXCollections.observableArrayList(
                "09:00", "09:30", "10:00", "10:30", "11:00",
                "11:30", "12:00", "12:30", "13:00", "13:30",
                "14:00", "14:30", "15:00", "15:30", "16:00",
                "16:30", "17:00"
        ));
        loadCandidates();
    }

    @FXML
    private void handleAddInterview() {
        try {
            Candidat selected = candidateComboBox.getValue();

            if (selected == null) {
                showAlert("Error", "Please select a candidate.");
                return;
            }

            int candidateId = selected.getId_candidat();

            LocalDate date = datePicker.getValue();
            if (date == null || timeComboBox.getValue() == null) {
                showAlert("Error", "Please select a date and time.");
                return;
            }
            LocalTime time = LocalTime.parse(timeComboBox.getValue());
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            String type = typeComboBox.getValue();
            String jobOffer = jobOfferField.getText();
            String interviewer = interviewerField.getText();

            Interview interview = new Interview(candidateId, dateTime, type, jobOffer, interviewer);
            interviewService.ajouter(interview);

            showAlert("Success", "Interview added successfully!");
            closeWindow();

        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) candidateComboBox.getScene().getWindow();

        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
