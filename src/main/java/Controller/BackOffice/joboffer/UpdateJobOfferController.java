package Controller.BackOffice.joboffer;

import Model.JobOffer;
import Service.JobOfferService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UpdateJobOfferController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextField skillsField;

    private JobOffer jobOffer;
    private JobOfferService service = new JobOfferService();

    @FXML
    public void initialize() {

        typeField.getItems().addAll(
                "Full-time",
                "Part-time",
                "Contract",
                "Remote"
        );

        statusField.getItems().addAll(
                "Active",
                "Pending",
                "Closed"
        );
    }

    public void setJobOffer(JobOffer offer) {
        this.jobOffer = offer;

        if (offer != null) {
            titleField.setText(offer.getTitle());
            descriptionField.setText(offer.getDescription());
            typeField.setValue(offer.getType());
            statusField.setValue(offer.getStatus());
            skillsField.setText(offer.getRequiredSkills());
        }
    }

    @FXML
    private void handleUpdate() {

        if (!validateInputs()) return;

        try {
            jobOffer.setTitle(titleField.getText().trim());
            jobOffer.setDescription(descriptionField.getText().trim());
            jobOffer.setType(typeField.getValue());
            jobOffer.setStatus(statusField.getValue());
            jobOffer.setRequiredSkills(skillsField.getText().trim());

            service.updateJobOffer(jobOffer);

            showAlert(Alert.AlertType.INFORMATION,
                    "Success",
                    "Job Updated",
                    "The job offer was successfully updated!");

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "Update Failed",
                    "Something went wrong: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {

        if (titleField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() ||
                typeField.getValue() == null ||
                statusField.getValue() == null ) {

            showAlert(Alert.AlertType.ERROR,
                    "Validation Error",
                    "Missing Fields",
                    "Please fill all required fields.");

            return false;
        }



        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
