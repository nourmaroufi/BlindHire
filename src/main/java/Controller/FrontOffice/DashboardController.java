package Controller.FrontOffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Button backOfficeButton;


    @FXML
    private void gotoBO() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backOfficeButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("BackOffice - Home");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}