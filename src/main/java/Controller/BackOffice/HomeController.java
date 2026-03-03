package Controller.BackOffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadPage("/BackOffice/DashboardContent.fxml");
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + fxml);
        }
    }

    @FXML
    private void goToDashboard() {
        loadPage("/BackOffice/DashboardContent.fxml");
    }

    @FXML
    private void goToJobOffers() {
        loadPage("/BackOffice/joboffer.fxml");
    }

    @FXML
    private void goToApplications() {
        loadPage("/Applications.fxml");
    }

    @FXML
    private void goToMessages() {
        loadPage("/Messages.fxml");
    }

    @FXML
    private void goToProfile() {
        loadPage("/Profile.fxml");
    }

    @FXML
    private void goToSettings() {
        loadPage("/Settings.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to logout?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Scene scene = new Scene(loader.load());

                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("BlindHire - Login");
                stage.setMaximized(true);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Could not load the login screen.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}