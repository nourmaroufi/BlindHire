package Controller.BackOffice.joboffer;

import Model.JobOffer;
import Service.JobOfferService;
import javafx.fxml.FXML;
import java.sql.SQLException;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UpdateJobOfferController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextField skillsField;
    @FXML private TextField salaryField;

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
        if (offer == null) return;

        // ✅ Always re-fetch from DB to guarantee all fields (incl. required_skills) are loaded
        try {
            JobOffer fresh = service.getJobOfferById(offer.getId());
            this.jobOffer = (fresh != null) ? fresh : offer;
        } catch (Exception e) {
            e.printStackTrace();
            this.jobOffer = offer; // fallback to passed object
        }

        titleField.setText(nullSafe(jobOffer.getTitle()));
        descriptionField.setText(nullSafe(jobOffer.getDescription()));
        typeField.setValue(jobOffer.getType());
        statusField.setValue(jobOffer.getStatus());
        skillsField.setText(nullSafe(jobOffer.getRequiredSkills()));
        if (jobOffer.getOfferedSalary() != null && jobOffer.getOfferedSalary() > 0)
            salaryField.setText(String.valueOf(jobOffer.getOfferedSalary().intValue()));
        else
            salaryField.clear();
    }

    private String nullSafe(String s) { return s != null ? s : ""; }

    @FXML
    private void handleUpdate() {

        if (!validateInputs()) return;

        try {
            jobOffer.setTitle(titleField.getText().trim());
            jobOffer.setDescription(descriptionField.getText().trim());
            jobOffer.setType(typeField.getValue());
            jobOffer.setStatus(statusField.getValue());
            jobOffer.setRequiredSkills(skillsField.getText().trim());
            String salaryText = salaryField != null ? salaryField.getText().trim() : "";
            if (!salaryText.isEmpty()) {
                try { jobOffer.setOfferedSalary(Double.parseDouble(salaryText)); }
                catch (NumberFormatException e) { /* ignore invalid */ }
            } else {
                jobOffer.setOfferedSalary(null);
            }

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