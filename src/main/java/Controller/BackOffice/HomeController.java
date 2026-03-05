package Controller.BackOffice;

import Model.User;
import Service.userservice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HomeController {

    @FXML private StackPane contentArea;

    // Sidebar profile labels (wired to home.fxml)
    @FXML private Label sidebarInitials;
    @FXML private Label sidebarUsername;
    @FXML private Label sidebarRole;

    @FXML
    public void initialize() {
        populateSidebarProfile();
        loadPage("/BackOffice/DashboardContent.fxml");
    }

    // ── Populate the sidebar user card from the session ───────────────────────

    private void populateSidebarProfile() {
        try {
            User user = new userservice().getCurrentUser();
            if (user == null) return;

            // Initials
            String nom    = user.getNom()    != null ? user.getNom()    : "";
            String prenom = user.getPrenom() != null ? user.getPrenom() : "";
            String initials = (!nom.isEmpty()    ? nom.substring(0,1).toUpperCase()    : "")
                    + (!prenom.isEmpty() ? prenom.substring(0,1).toUpperCase() : "");
            sidebarInitials.setText(initials.isEmpty() ? "?" : initials);

            // Display name (blind username if set, else "First Last")
            sidebarUsername.setText(user.getDisplayName());

            // Role badge
            String roleStr = user.getRole() != null ? capitalize(user.getRole().name()) : "Admin";
            sidebarRole.setText(roleStr);

            // Tint the role badge by role
            String badgeColor = switch (roleStr.toLowerCase()) {
                case "admin"     -> "rgba(99,102,241,0.3)";
                case "recruteur" -> "rgba(6,182,212,0.3)";
                default          -> "rgba(16,185,129,0.3)";
            };
            String textColor = switch (roleStr.toLowerCase()) {
                case "admin"     -> "#a5b4fc";
                case "recruteur" -> "#67e8f9";
                default          -> "#6ee7b7";
            };
            sidebarRole.setStyle(
                    "-fx-background-color: " + badgeColor + ";" +
                            "-fx-text-fill: " + textColor + ";" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 2 8;" +
                            "-fx-background-radius: 8;"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

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
    private void goToUsers() {
        // Load the Users management view — adjust path to your actual FXML
        loadPage("/BackOffice/Users.fxml");
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
                // Clear session
                new userservice().setCurrentUser(null);

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}