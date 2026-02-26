package Controller.FrontOffice;
import Controller.FrontOffice.UpdateCandidatureController;
import Model.Candidature;
import Service.CandidatureService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class MyApplicationsController {

    @FXML private VBox applicationsContainer;
    @FXML private Label countLabel;

    private HomeController homeController;
    private final CandidatureService candidatureService = new CandidatureService();

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        loadApplications();
    }

    private void loadApplications() {
        try {
            List<Candidature> candidatures = candidatureService.getAllCandidatures();

            applicationsContainer.getChildren().clear();

            if (candidatures.isEmpty()) {
                Label empty = new Label("You have not submitted any applications yet.");
                empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
                applicationsContainer.getChildren().add(empty);
            }

            for (Candidature c : candidatures) {
                VBox card = createApplicationCard(c);
                applicationsContainer.getChildren().add(card);
            }

            countLabel.setText(candidatures.size() + " Applications");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createApplicationCard(Candidature c) {
        VBox card = new VBox(15);
        card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 20;
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            -fx-border-color: #e2e8f0;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,2);
            """);

        card.setOnMouseEntered(e ->
                card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 20;
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            -fx-border-color: #4f46e5;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.15), 15,0,0,5);
            """)
        );
        card.setOnMouseExited(e ->
                card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 20;
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            -fx-border-color: #e2e8f0;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,2);
            """)
        );
        HBox topRow = new HBox();
        topRow.setSpacing(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label jobIcon = new Label("📋");
        jobIcon.setStyle("-fx-font-size: 20px;");

        Label jobLabel = new Label("Job ID: " + c.getJobOfferId());
        jobLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        titleBox.getChildren().addAll(jobIcon, jobLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(c.getStatus());
        statusLabel.setStyle("""
            -fx-padding: 5 15;
            -fx-background-radius: 20;
            -fx-font-weight: bold;
            -fx-font-size: 13px;
            """ + getStatusStyle(c.getStatus()));

        topRow.getChildren().addAll(titleBox, spacer, statusLabel);
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(10);
        detailsGrid.setStyle("-fx-padding: 10 0 5 0;");

        Label salaryIcon = new Label("💰");
        salaryIcon.setStyle("-fx-font-size: 14px;");
        Label salaryLabel = new Label("Expected Salary: " + c.getExpectedSalary());
        salaryLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px;");
        HBox salaryBox = new HBox(8, salaryIcon, salaryLabel);
        salaryBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 14px;");
        Label dateLabel = new Label("Applied: " + c.getApplicationDate());
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        HBox dateBox = new HBox(8, dateIcon, dateLabel);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        detailsGrid.add(salaryBox, 0, 0);
        detailsGrid.add(dateBox, 1, 0);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e2e8f0;");

        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setStyle("-fx-padding: 5 0 0 0;");

        Button updateBtn = new Button("✏️ Update");
        updateBtn.setStyle("""
            -fx-background-color: white;
            -fx-text-fill: #4f46e5;
            -fx-border-color: #4f46e5;
            -fx-border-width: 2;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-padding: 8 20;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """);
        updateBtn.setOnAction(e -> handleUpdateApplication(c));

        updateBtn.setOnMouseEntered(e ->
                updateBtn.setStyle("""
            -fx-background-color: #4f46e5;
            -fx-text-fill: white;
            -fx-border-color: #4f46e5;
            -fx-border-width: 2;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-padding: 8 20;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 8,0,0,2);
            """)
        );
        updateBtn.setOnMouseExited(e ->
                updateBtn.setStyle("""
            -fx-background-color: white;
            -fx-text-fill: #4f46e5;
            -fx-border-color: #4f46e5;
            -fx-border-width: 2;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-padding: 8 20;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """)
        );

        Button cancelBtn = new Button("🗑️ Cancel");
        cancelBtn.setStyle("""
            -fx-background-color: white;
            -fx-text-fill: #dc2626;
            -fx-border-color: #dc2626;
            -fx-border-width: 2;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-padding: 8 20;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """);
        cancelBtn.setOnAction(e -> handleCancelApplication(c));

        cancelBtn.setOnMouseEntered(e ->
                cancelBtn.setStyle("""
            -fx-background-color: #dc2626;
            -fx-text-fill: white;
            -fx-border-color: #dc2626;
            -fx-border-width: 2;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-padding: 8 20;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(220,38,38,0.3), 8,0,0,2);
            """)
        );
        cancelBtn.setOnMouseExited(e ->
                cancelBtn.setStyle("""
            -fx-background-color: white;
            -fx-text-fill: #dc2626;
            -fx-border-color: #dc2626;
            -fx-border-width: 2;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-padding: 8 20;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """)
        );

        actionButtons.getChildren().addAll(updateBtn, cancelBtn);

        card.getChildren().addAll(topRow, detailsGrid, separator, actionButtons);

        return card;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        return switch (status.toLowerCase()) {
            case "pending" -> "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;";
            case "accepted" -> "-fx-background-color: #d1fae5; -fx-text-fill: #059669;";
            case "rejected" -> "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;";
            default -> "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        };
    }

    private void handleUpdateApplication(Candidature c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/UpdateCandidature.fxml")
            );

            Parent root = loader.load();

            UpdateCandidatureController controller = loader.getController();
            controller.setCandidature(c);

            Stage stage = new Stage();
            stage.setTitle("Update Application");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh after closing popup
            loadApplications();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCancelApplication(Candidature c) {
        try {
            candidatureService.deleteCandidature(c.getId());

            loadApplications();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleBack() {
        if (homeController != null) {
            homeController.handleJobs();
        }
    }
}

