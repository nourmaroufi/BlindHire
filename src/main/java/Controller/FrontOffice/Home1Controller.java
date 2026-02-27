package Controller.FrontOffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class Home1Controller {
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
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadDashboard();
    }
    @FXML
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/DashboardContent1.fxml")
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


            System.out.println("job offers clicked");}



        public void loadJobDetails() {

            System.out.println("job offers clicked");
        }
    @FXML
    private void gotoCandidatInterviews() {
        loadPage("/CandidatInterview.fxml");
    }
    @FXML
    private void gotoPracticeInterview() {
        loadPage("/PracticeInterview.fxml");
    }




    }