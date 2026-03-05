package ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BlindHireApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("BlindHire - RH Agency");

        // Show splash first; real navigation happens in navigateAfterSplash()
        SplashScreen splash = new SplashScreen(
                () -> javafx.application.Platform.runLater(this::navigateAfterSplash)
        );
        primaryStage.setScene(new Scene(splash.getRoot(), 960, 540));
        primaryStage.setMaximized(false);
        primaryStage.show();
    }

    /**
     * Called once the splash animation finishes.
     * Contains the original start() logic unchanged.
     */
    private void navigateAfterSplash() {
        // ── Check for a saved "Remember Me" session ───────────────────────────
        int savedId = Utils.SessionManager.getSavedUserId();
        if (savedId >= 0) {
            try {
                Service.userservice svc = new Service.userservice();
                Model.User saved = svc.getUserById(savedId);
                if (saved != null && saved.isVerified()) {
                    svc.setCurrentUser(saved);
                    Parent root = (saved.getRole() == Model.Role.admin || saved.getRole() == Model.Role.recruteur)
                            ? new DashboardPage().getRoot()
                            : new HomePage(saved).getRoot();
                    loadSceneFullscreen(root);
                    return;
                }
            } catch (Exception e) {
                System.err.println("[Session] Auto-login failed: " + e.getMessage());
            }
            // Session was stale or user not found — clear it
            Utils.SessionManager.clearSession();
        }

        // No session — show Welcome page
        loadScene(new WelcomePage().getRoot(), 960, 540);
    }
    public static void loadScene(Parent root, int width, int height) {
        // For small pages like WelcomePage, LoginPage, SignupPage
        Scene scene = new Scene(root, width, height);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false); // ← keep small
    }

    public static void loadSceneFullscreen(Parent root) {
        // For HomePage, DashboardPage, ProfilePage etc.
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}