package Controller.BackOffice.Candidature;

import Model.Candidature;
import Model.Candidat;
import Service.CandidatureService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.net.URL;
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

    private int jobOfferFilterId = -1;

    public void setJobOfferFilter(int jobOfferId) {
        this.jobOfferFilterId = jobOfferId;
        loadApplications(); // reload with filter
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
                Candidat c = new CandidatureService().getCandidatById(cell.getValue().getCandidateId());
                return new ReadOnlyStringWrapper(c != null ? c.getName() : "Unknown");
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
                Candidat c = new CandidatureService().getCandidatById(cell.getValue().getCandidateId());
                return new ReadOnlyStringWrapper(c != null && c.getExperiences() != null ?
                        c.getExperiences() : "n/a");
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
                    setStyle(candidature.getStatusBadgeStyle() +
                            "-fx-font-weight: bold; -fx-alignment: center; -fx-padding: 8;");
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button acceptBtn = new Button("✓");
            private final Button rejectBtn = new Button("✗");
            private final HBox pane = new HBox(8);

            {
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");
                acceptBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");
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
        long total = applicationsTable.getItems().size();
        long pending = applicationsTable.getItems().stream()
                .filter(app -> "pending".equalsIgnoreCase(app.getStatus())).count();
        long accepted = applicationsTable.getItems().stream()
                .filter(app -> "accepted".equalsIgnoreCase(app.getStatus())).count();
        long rejected = applicationsTable.getItems().stream()
                .filter(app -> "rejected".equalsIgnoreCase(app.getStatus())).count();

        totalApplicationsLabel.setText(String.valueOf(total));
        pendingApplicationsLabel.setText(String.valueOf(pending));
        acceptedApplicationsLabel.setText(String.valueOf(accepted));
        rejectedApplicationsLabel.setText(String.valueOf(rejected));
    }

    private void updatePaginationInfo() {
        int size = applicationsTable.getItems().size();
        if (size > 0) {
            paginationInfoLabel.setText("Showing 1-" + Math.min(10, size) + " of " + size + " applications");
        } else {
            paginationInfoLabel.setText("No applications found");
        }
    }

    private void handleViewApplication(Candidature c) throws SQLException {
        Candidat candidate = new CandidatureService().getCandidatById(c.getCandidateId());
        String jobTitle = new CandidatureService().getJobTitleById(c.getJobOfferId());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Application details");
        alert.setHeaderText((candidate != null ? candidate.getName() : "Unknown") + " - " + jobTitle);

        String content = String.format(
                "Candidate: %s\nPosition: %s\nApplied: %s\nExperience: %s\nStatus: %s",
                candidate != null ? candidate.getName() : "Unknown",
                jobTitle,
                c.getApplicationDate(),
                candidate != null ? candidate.getExperiences() : "n/a",
                c.getStatus()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }
    private void handleAcceptApplication(Candidature c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accept application");
        confirm.setHeaderText("Accept candidate ID " + c.getCandidateId());
        confirm.setContentText("Are you sure you want to accept this application?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                c.setStatus("accepted");

                // Update in database
                try {
                    new CandidatureService().updateCandidature(c);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Failed to update status in the database.");
                    return;
                }

                applicationsTable.refresh();
                updateStatistics();
                showSuccessAlert("Application accepted", "Candidate ID " + c.getCandidateId() + " accepted successfully!");
            }
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
                if (response == ButtonType.OK) {
                    c.setStatus("rejected");
                    c.setRejectionReason(reason);

                    // Update in database
                    try {
                        new CandidatureService().updateCandidature(c);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showErrorAlert("Database Error", "Failed to update status in the database.");
                        return;
                    }

                    applicationsTable.refresh();
                    updateStatistics();
                    showSuccessAlert("Application rejected", "Candidate ID " + c.getCandidateId() + " rejected.\nReason: " + reason);
                }
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
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle(title);
        success.setHeaderText(null);
        success.setContentText(message);
        success.showAndWait();
    }

}