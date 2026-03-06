package Controller.BackOffice.Candidature;

import Model.Candidature;
import Model.User;
import Service.CandidatureService;
import Service.EmailService;
import Service.JobOfferService;
import Service.NotificationCService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import javafx.stage.FileChooser;
import Service.CandidaturePdfExportService;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CandidatureController implements Initializable {

    @FXML private TableView<Candidature> applicationsTable;
    @FXML private TableColumn<Candidature, String> candidateNameColumn;
    @FXML private TableColumn<Candidature, String> jobPositionColumn;
    @FXML private TableColumn<Candidature, String> appliedDateColumn;
    @FXML private TableColumn<Candidature, String> statusColumn;
    @FXML private TableColumn<Candidature, String> experienceColumn;
    @FXML private TableColumn<Candidature, Void> actionsColumn;

    @FXML private Label totalApplicationsLabel;
    @FXML private Label pendingApplicationsLabel;
    @FXML private Label acceptedApplicationsLabel;
    @FXML private Label rejectedApplicationsLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> jobPositionFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label paginationInfoLabel;
    @FXML private Button exportPdfBtn;

    private int jobOfferFilterId = -1;

    public void setJobOfferFilter(int jobOfferId) {
        this.jobOfferFilterId = jobOfferId;
        loadApplications();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFilters();
        loadApplications();
        updateStatistics();
    }

    private void setupTableColumns() {
        candidateNameColumn.setCellValueFactory(cell -> {
            try {
                User c = new CandidatureService().getCandidateUserById(cell.getValue().getCandidateId());
                return new ReadOnlyStringWrapper(c != null && c.getUsername() != null ? c.getUsername() : "Anonymous");
            } catch (SQLException e) {
                e.printStackTrace();
                return new ReadOnlyStringWrapper("Error");
            }
        });

        jobPositionColumn.setCellValueFactory(cell -> {
            try {
                String title = new CandidatureService().getJobTitleById(cell.getValue().getJobOfferId());
                return new ReadOnlyStringWrapper(title != null ? title : "Unknown");
            } catch (SQLException e) {
                e.printStackTrace();
                return new ReadOnlyStringWrapper("Error");
            }
        });

        appliedDateColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getApplicationDate() != null ?
                        cell.getValue().getApplicationDate().toString() : "")
        );

        experienceColumn.setCellValueFactory(cell -> {
            try {
                User c = new CandidatureService().getCandidateUserById(cell.getValue().getCandidateId());
                return new ReadOnlyStringWrapper(c != null && c.getExperience() != null ?
                        c.getExperience() : "n/a");
            } catch (SQLException e) {
                e.printStackTrace();
                return new ReadOnlyStringWrapper("n/a");
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Candidature candidature = (Candidature) getTableRow().getItem();
                    setText(candidature.getStatus().substring(0, 1).toUpperCase() +
                            candidature.getStatus().substring(1).toLowerCase());
                    String s = candidature.getStatus().toLowerCase();
                    String bg   = s.equals("accepted") ? "rgba(16,185,129,0.18)"
                            : s.equals("rejected") ? "rgba(244,63,94,0.18)"
                            : "rgba(245,158,11,0.18)";
                    String fg   = s.equals("accepted") ? "#10b981"
                            : s.equals("rejected") ? "#f43f5e"
                            : "#f59e0b";
                    setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                            "-fx-background-radius: 20; -fx-padding: 4 12;" +
                            "-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center;");
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn   = new Button("👁");
            private final Button acceptBtn = new Button("✓");
            private final Button rejectBtn = new Button("✗");
            private final HBox pane = new HBox(8);

            {
                viewBtn.setStyle("-fx-background-color: rgba(99,102,241,0.25); -fx-text-fill: #818cf8; -fx-background-radius: 8; -fx-border-color: rgba(99,102,241,0.40); -fx-border-width: 1; -fx-border-radius: 8; -fx-min-width: 34; -fx-min-height: 34; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;");
                acceptBtn.setStyle("-fx-background-color: rgba(16,185,129,0.20); -fx-text-fill: #10b981; -fx-background-radius: 8; -fx-border-color: rgba(16,185,129,0.40); -fx-border-width: 1; -fx-border-radius: 8; -fx-min-width: 34; -fx-min-height: 34; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;");
                rejectBtn.setStyle("-fx-background-color: rgba(244,63,94,0.20); -fx-text-fill: #f43f5e; -fx-background-radius: 8; -fx-border-color: rgba(244,63,94,0.40); -fx-border-width: 1; -fx-border-radius: 8; -fx-min-width: 34; -fx-min-height: 34; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;");
                pane.getChildren().addAll(viewBtn, acceptBtn, rejectBtn);
                pane.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(event -> {
                    try {
                        handleViewApplication(getTableView().getItems().get(getIndex()));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                acceptBtn.setOnAction(event -> handleAcceptApplication(getTableView().getItems().get(getIndex())));
                rejectBtn.setOnAction(event -> handleRejectApplication(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        jobPositionFilter.getItems().addAll("All positions", "Senior Developer", "UX Designer",
                "Product Manager", "Data Analyst", "DevOps Engineer");
        jobPositionFilter.setValue("All positions");

        statusFilter.getItems().addAll("All status", "Pending", "Accepted", "Rejected");
        statusFilter.setValue("All status");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterApplications());
        jobPositionFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterApplications());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterApplications());
    }

    private void filterApplications() {
        String searchText = searchField.getText().toLowerCase();
        String selectedJob = jobPositionFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        System.out.println("Filtering applications - search: " + searchText +
                ", job: " + selectedJob + ", status: " + selectedStatus);
    }

    private void loadApplications() {
        applicationsTable.getItems().clear();
        CandidatureService service = new CandidatureService();

        try {
            List<Candidature> candidatures;
            if (jobOfferFilterId != -1) {
                candidatures = service.getCandidaturesByJobOfferId(jobOfferFilterId);
            } else {
                candidatures = service.getAllCandidatures();
            }
            applicationsTable.getItems().addAll(candidatures);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updatePaginationInfo();
        updateStatistics();
    }

    private void updateStatistics() {
        long total    = applicationsTable.getItems().size();
        long pending  = applicationsTable.getItems().stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus())).count();
        long accepted = applicationsTable.getItems().stream().filter(a -> "accepted".equalsIgnoreCase(a.getStatus())).count();
        long rejected = applicationsTable.getItems().stream().filter(a -> "rejected".equalsIgnoreCase(a.getStatus())).count();

        totalApplicationsLabel.setText(String.valueOf(total));
        pendingApplicationsLabel.setText(String.valueOf(pending));
        acceptedApplicationsLabel.setText(String.valueOf(accepted));
        rejectedApplicationsLabel.setText(String.valueOf(rejected));
    }

    private void updatePaginationInfo() {
        int size = applicationsTable.getItems().size();
        paginationInfoLabel.setText(size > 0
                ? "Showing 1-" + Math.min(10, size) + " of " + size + " applications"
                : "No applications found");
    }

    private void handleViewApplication(Candidature c) throws SQLException {
        try {
            // Load the FXML for the details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/CandidatureDetails.fxml"));
            Parent root = loader.load();

            // Get the controller and set the candidature
            CandidatureDetailsController detailsController = loader.getController();
            detailsController.setCandidature(c, this);

            // Create a new stage for the popup
            Stage stage = new Stage();
            stage.setTitle("Application Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Show the popup
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to old alert if FXML loading fails
            showSimpleAlert(c);
        }
    }

    // Add this public method to allow refreshing from details controller
    public void refreshApplications() {
        loadApplications();
    }
    // Fallback method if FXML loading fails
    private void showSimpleAlert(Candidature c) throws SQLException {
        User candidate = new CandidatureService().getCandidateUserById(c.getCandidateId());
        String jobTitle = new CandidatureService().getJobTitleById(c.getJobOfferId());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Application details");
        alert.setHeaderText((candidate != null ? candidate.getUsername() != null ? candidate.getUsername() : "Anonymous" : "Unknown") + " - " + jobTitle);
        alert.setContentText(String.format(
                "Candidate: %s\nPosition: %s\nApplied: %s\nExperience: %s\nStatus: %s",
                candidate != null ? candidate.getUsername() != null ? candidate.getUsername() : "Anonymous" : "Unknown",
                jobTitle,
                c.getApplicationDate(),
                candidate != null ? candidate.getExperience() : "n/a",
                c.getStatus()
        ));
        alert.showAndWait();
    }
    private void handleAcceptApplication(Candidature c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accept application");
        confirm.setHeaderText("Accept candidate " + c.getCandidateId());
        confirm.setContentText("Are you sure you want to accept this application?");

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            CandidatureService service = new CandidatureService();

            // ── 1. Accept this candidature only ──────────────────────────────
            try {
                service.acceptAndRejectOthers(c.getId(), c.getJobOfferId());
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Database Error", "Failed to update status in the database.");
                return;
            }

            // ── 2. Close the job offer ────────────────────────────────────────
            try {
                new JobOfferService().closeJobOffer(c.getJobOfferId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ── 3. Send acceptance email + in-app notification ────────────────
            try {
                User candidate = service.getCandidateUserById(c.getCandidateId());
                String jobTitle = service.getJobTitleById(c.getJobOfferId());
                if (candidate != null) {
                    String displayName = candidate.getUsername() != null ? candidate.getUsername() : "Candidate";
                    if (candidate.getEmail() != null) {
                        new EmailService().sendEmail(
                                candidate.getEmail(),
                                "Your application has been accepted – " + jobTitle,
                                "Dear " + displayName + ",\n\n" +
                                        "Congratulations! We are pleased to inform you that your application for \"" + jobTitle + "\" has been accepted.\n\n" +
                                        "Our team will be in touch with you shortly regarding the next steps.\n\n" +
                                        "Best regards,\nBlindHire Team"
                        );
                    }
                    new NotificationCService().createNotification(
                            candidate.getId(),
                            "accepted",
                            "🎉 Application Accepted — " + jobTitle,
                            "Congratulations! Your application for \"" + jobTitle + "\" has been accepted. Click to take your quiz.",
                            c.getJobOfferId()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ── 4. Reload the table ───────────────────────────────────────────
            loadApplications();
            showSuccessAlert("Application accepted", "Candidate accepted successfully.");
        });
    }


    private void handleRejectApplication(Candidature c) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject application");
        dialog.setHeaderText("Reject candidate ID " + c.getCandidateId());
        dialog.setContentText("Provide a reason for rejection:");

        dialog.showAndWait().ifPresent(reason -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm rejection");
            confirm.setHeaderText("Reject candidate ID " + c.getCandidateId());
            confirm.setContentText("Are you sure you want to reject this application?\nReason: " + reason);

            confirm.showAndWait().ifPresent(response -> {
                if (response != ButtonType.OK) return;

                c.setStatus("rejected");
                c.setRejectionReason(reason);

                try {
                    new CandidatureService().updateCandidature(c);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Failed to update status in the database.");
                    return;
                }

                try {
                    CandidatureService service = new CandidatureService();
                    User candidate = service.getCandidateUserById(c.getCandidateId());
                    String jobTitle = service.getJobTitleById(c.getJobOfferId());
                    if (candidate != null && candidate.getEmail() != null) {
                        new EmailService().sendEmail(
                                candidate.getEmail(),
                                "Update on your application – " + jobTitle,
                                "Dear " + candidate.getUsername() != null ? candidate.getUsername() : "Anonymous" + ",\n\n" +
                                        "Thank you for applying for \"" + jobTitle + "\".\n\n" +
                                        "After careful consideration, we regret to inform you that your application has not been selected at this time.\n\n" +
                                        "Reason: " + reason + "\n\n" +
                                        "We encourage you to apply for future opportunities.\n\n" +
                                        "Best regards,\nBlindHire Team"
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                loadApplications();
                showSuccessAlert("Application rejected",
                        "Candidate ID " + c.getCandidateId() + " rejected.\nReason: " + reason);
            });
        });
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // ── EXPORT PDF ────────────────────────────────────────────────────────────

    @FXML
    private void handleExportPdf() {
        java.util.List<Candidature> items = applicationsTable.getItems();
        if (items == null || items.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export PDF"); a.setHeaderText(null);
            a.setContentText("No applications to export."); a.showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Applications PDF");
        chooser.setInitialFileName("candidatures.pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        Stage stage = (Stage) exportPdfBtn.getScene().getWindow();
        File dest = chooser.showSaveDialog(stage);
        if (dest == null) return;

        exportPdfBtn.setDisable(true);
        exportPdfBtn.setText("⏳  Exporting...");

        new Thread(() -> {
            try {
                CandidatureService svc = new CandidatureService();
                CandidaturePdfExportService.export(items, svc, dest.getAbsolutePath());

                javafx.application.Platform.runLater(() -> {
                    exportPdfBtn.setDisable(false);
                    exportPdfBtn.setText("📄  Export PDF");
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Export Successful"); ok.setHeaderText(null);
                    ok.setContentText("PDF saved:\n" + dest.getAbsolutePath());
                    ok.showAndWait();
                    try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    exportPdfBtn.setDisable(false);
                    exportPdfBtn.setText("📄  Export PDF");
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Export Failed"); err.setHeaderText(null);
                    err.setContentText("Could not generate PDF:\n" + e.getMessage());
                    err.showAndWait();
                });
            }
        }).start();
    }


}