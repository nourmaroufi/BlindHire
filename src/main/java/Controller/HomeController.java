package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;


public class HomeController {


    @FXML
    private StackPane contentArea;


    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private ListView<String> calendarList;
    @FXML
    public void initialize() {
        loadPage("/DashboardContent.fxml");
        calendarList.setItems(FXCollections.observableArrayList(
                "10 AM - Interview",
                "12 PM - Team Meeting",
                "3 PM - Training"
        ));
    }

    @FXML
    private void goToAddJob(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addjoboffer.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BlindHire - Add Job Offer");
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the Add Job form.");
        }
    }

    @FXML
    private void goToJobOffers(ActionEvent event) {
        loadPage("/joboffer.fxml");

    }

    @FXML
    private void gotodashboard() {
        loadPage("/DashboardContent.fxml");
    }

    @FXML
    private void gotoInterviews() {
        loadPage("/interview.fxml");
    }

    @FXML
    private void gotocandidat() {
        loadPage("/candidat.fxml");
    }


    @FXML
    private void logout(javafx.event.ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText("You will be returned to the login screen.");

        ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {
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
                showAlert("Error", "Could not load the Login screen.");
            }
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}