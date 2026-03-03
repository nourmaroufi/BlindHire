package Controller.BackOffice.joboffer;
import javafx.scene.control.ComboBox;
import Model.JobOffer;
import Service.JobOfferService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.time.LocalDate;

public class addJobOfferController {
    private JobOfferListController listController;

    public void setListController(JobOfferListController controller) {
        this.listController = controller;
    }
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField recruiterField;
    @FXML
    private ComboBox<String> typeField;


    private JobOfferService service = new JobOfferService();

    @FXML
    public void initialize() {
        typeField.getItems().addAll(
                "Full-time",
                "Part-time",
                "Contract",
                "Remote"
        );

    }


    @FXML
    private void handleAddJobOffer() {
        if (!validateInputs()) return;

        try {
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Add Job Offer");
            confirmAlert.setHeaderText("Add New Job Offer");
            confirmAlert.setContentText("Are you sure you want to add this job offer?");

            ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);
            if (result != ButtonType.OK) return;

            LocalDate postingDate = LocalDate.now();
            String status = "Pending";

            JobOffer job = new JobOffer(
                    titleField.getText().trim(),
                    descriptionField.getText().trim(),
                    Integer.parseInt(recruiterField.getText().trim()),
                    typeField.getValue(),
                    status,
                    postingDate
            );

            service.addJobOffer(job);

            showAlert(AlertType.INFORMATION, "Success", "Job Offer Added",
                    "The job offer has been successfully added!");
            if (listController != null) {
                listController.refresh();
            }


            clearForm();

        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Input Error", "Invalid Recruiter ID",
                    "Please enter a valid numeric Recruiter ID.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to Add Job Offer",
                    "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (hasUnsavedChanges()) {
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Cancel");
            confirmAlert.setHeaderText("Unsaved Changes");
            confirmAlert.setContentText("You have unsaved changes. Are you sure you want to cancel?");
            ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);
            if (result == ButtonType.OK) closeWindow(event);
        } else {
            closeWindow(event);
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (titleField.getText() == null || titleField.getText().trim().isEmpty())
            errorMessage.append("- Job Title is required\n");

        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty())
            errorMessage.append("- Description is required\n");



        if (recruiterField.getText() == null || recruiterField.getText().trim().isEmpty()) {
            errorMessage.append("- Recruiter ID is required\n");
        } else {
            try {
                Integer.parseInt(recruiterField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage.append("- Recruiter ID must be a valid number\n");
            }
        }

        if (typeField.getValue() == null)
            errorMessage.append("- Job Type is required\n");




        if (errorMessage.length() > 0) {
            showAlert(AlertType.ERROR, "Validation Error", "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    private boolean hasUnsavedChanges() {
        return (titleField.getText() != null && !titleField.getText().trim().isEmpty()) ||
                (descriptionField.getText() != null && !descriptionField.getText().trim().isEmpty()) ||
                (recruiterField.getText() != null && !recruiterField.getText().trim().isEmpty()) ||
                (typeField.getValue() != null) ;

    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        recruiterField.clear();
        typeField.setValue(null);

    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}