package Controller.FrontOffice;
import Model.JobOffer;
import Service.candidatService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import Service.AiMatchingService;
import javafx.scene.control.Alert;
import java.io.IOException;

public class HomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadDashboard();
    }
    @FXML
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/Dashboard.fxml")
            );
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMyApplications() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/MyApplications.fxml")
            );

            Parent view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public StackPane getContentArea() {
        return contentArea;
    }

    // Optional button handlers
    @FXML
    private void handleSignIn() {
        System.out.println("Sign In clicked");
    }

    @FXML
    private void handleSignUp() {
        System.out.println("Sign Up clicked");
    }

    @FXML
    public void handleJobs() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/JobOffer.fxml")
            );

            Parent view = loader.load();

            jobController controller = loader.getController();
            controller.setHomeController(this);

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadJobDetails(JobOffer job) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/JobDetails.fxml")
            );

            Parent view = loader.load();

            JobDetailsController controller = loader.getController();
            controller.setJob(job);
            controller.setHomeController(this);

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
