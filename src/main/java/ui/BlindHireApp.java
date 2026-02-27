package ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BlindHireApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("BlindHire - RH Agency");

        // Check for a saved "Remember Me" session
        int savedId = Utils.SessionManager.getSavedUserId();
        if (savedId >= 0) {
            Service.userservice svc = new Service.userservice();
            Model.User saved = svc.getUserById(savedId);
            if (saved != null && saved.isVerified()) {
                svc.setCurrentUser(saved);
                Parent root = saved.getRole() == Model.Role.admin
                        ? new DashboardPage().getRoot()
                        : new HomePage(saved).getRoot();
                primaryStage.setScene(new Scene(root, 960, 540));
                primaryStage.show();
                return;
            } else {
                // Stale session — clear it
                Utils.SessionManager.clearSession();
            }
        }

        // No valid session — show welcome page
        WelcomePage welcomePage = new WelcomePage();
        Scene scene = new Scene(welcomePage.getRoot(), 960, 540);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void loadScene(Parent root, int width, int height) {
        Scene scene = new Scene(root, width, height);
        primaryStage.setScene(scene);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}