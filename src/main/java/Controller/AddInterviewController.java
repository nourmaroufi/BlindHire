package Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Candidat;
import model.Interview;
import services.CandidatService;
import services.InterviewService;
import services.NominatimService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.scene.web.WebView;

public class AddInterviewController {

    @FXML private ComboBox<Candidat> candidateComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField jobOfferField;
    @FXML private TextField interviewerField;
    @FXML private TextField locationField;
    @FXML private Label locationLabel;
    @FXML private Button searchMapBtn;
    @FXML private WebView mapView;
    @FXML private Label mapLabel;

    private NominatimService nominatimService = new NominatimService();
    private InterviewService interviewService = new InterviewService();
    private CandidatService candidatService = new CandidatService();
    private double[] currentCoords = null;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Online", "In Person"));
        timeComboBox.setItems(FXCollections.observableArrayList(
                "09:00","09:30","10:00","10:30","11:00","11:30",
                "12:00","12:30","13:00","13:30","14:00","14:30",
                "15:00","15:30","16:00","16:30","17:00"
        ));
        loadCandidates();

        // Block past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
                if (date.isBefore(LocalDate.now())) {
                    setStyle("-fx-background-color:#f0f0f0; -fx-text-fill:#ccc;");
                }
            }
        });

        // Hide all location fields initially
        hideLocationRow();

        // Show/hide based on type selection
        typeComboBox.setOnAction(e -> {
            String selected = typeComboBox.getValue();
            if ("Online".equals(selected)) {
                // Online: no location needed
                hideLocationRow();
                locationField.clear();
            } else if ("In Person".equals(selected)) {
                // In Person: show address + search + map
                locationLabel.setText("Address");
                locationField.setPromptText("Type an address to search...");
                locationField.clear();
                showLocationRow();
            }
        });
    }

    private void hideLocationRow() {
        locationLabel.setVisible(false);
        locationLabel.setManaged(false);
        locationField.setVisible(false);
        locationField.setManaged(false);
        searchMapBtn.setVisible(false);
        searchMapBtn.setManaged(false);
        mapView.setVisible(false);
        mapView.setManaged(false);
        mapLabel.setVisible(false);
        mapLabel.setManaged(false);
    }

    private void showLocationRow() {
        locationLabel.setVisible(true);
        locationLabel.setManaged(true);
        locationField.setVisible(true);
        locationField.setManaged(true);
        searchMapBtn.setVisible(true);
        searchMapBtn.setManaged(true);
        // map stays hidden until search is done
    }

    @FXML
    private void handleSearchLocation() {
        String address = locationField.getText().trim();
        if (address.isEmpty()) {
            showAlert("Error", "Please type an address first.");
            return;
        }

        new Thread(() -> {
            double[] coords = nominatimService.searchLocation(address);
            javafx.application.Platform.runLater(() -> {
                if (coords == null) {
                    showAlert("Not Found", "Address not found. Try being more specific.");
                    return;
                }

                currentCoords = coords;
                double lat = coords[0];
                double lon = coords[1];

                double offset = 0.005;
                String mapUrl = String.format(
                        "https://www.openstreetmap.org/export/embed.html" +
                                "?bbox=%.6f,%.6f,%.6f,%.6f&layer=mapnik&marker=%.6f,%.6f",
                        lon - offset, lat - offset,
                        lon + offset, lat + offset,
                        lat, lon
                );

                mapView.getEngine().load(mapUrl);
                mapView.setVisible(true);
                mapView.setManaged(true);
                mapLabel.setVisible(true);
                mapLabel.setManaged(true);

                String locationLink = String.format(
                        "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=17/%.6f/%.6f",
                        lat, lon, lat, lon
                );
                locationField.setText(locationLink);
            });
        }).start();
    }

    @FXML
    private void handleAddInterview() {
        try {
            Candidat selected = candidateComboBox.getValue();
            if (selected == null) { showAlert("Error", "Please select a candidate."); return; }

            LocalDate date = datePicker.getValue();
            if (date == null || timeComboBox.getValue() == null) {
                showAlert("Error", "Please select a date and time."); return;
            }

            String type = typeComboBox.getValue();
            if (type == null) { showAlert("Error", "Please select a type."); return; }

            if (jobOfferField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter a job offer."); return;
            }
            if (interviewerField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter an interviewer name."); return;
            }

            // Location only required for In Person
            String locationLink = null;
            if ("In Person".equals(type)) {
                locationLink = locationField.getText().trim();
                if (locationLink.isEmpty()) locationLink = null;
            }

            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(timeComboBox.getValue()));
            Interview interview = new Interview(
                    selected.getId_candidat(), dateTime, type,
                    jobOfferField.getText().trim(),
                    interviewerField.getText().trim(),
                    locationLink
            );

            interviewService.ajouter(interview);
            showAlert("Success", "Interview scheduled successfully!");
            closeWindow();

        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void loadCandidates() {
        try {
            candidateComboBox.getItems().addAll(candidatService.afficherAll());
        } catch (SQLException e) {
            showAlert("Error", "Failed to load candidates: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        ((Stage) candidateComboBox.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
