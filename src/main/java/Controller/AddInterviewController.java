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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
public class AddInterviewController {

    @FXML private ComboBox<Candidat> candidateComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField jobOfferField;
    @FXML private TextField interviewerField;
    @FXML private TextField locationField;
    @FXML private Label locationLabel;
   // @FXML private Button generateMapBtn;
    @FXML private WebView mapView;
    @FXML private Label mapLabel;
    private NominatimService nominatimService = new NominatimService();
    private double[] currentCoords = null; // stores [lat, lon]

    private InterviewService interviewService = new InterviewService();
    private CandidatService candidatService = new CandidatService();

    @FXML
    public void initialize() {
        mapView.setVisible(false);
        mapView.setManaged(false);
        mapLabel.setVisible(false);
        mapLabel.setManaged(false);

        typeComboBox.setItems(FXCollections.observableArrayList("Online", "In Person"));
        timeComboBox.setItems(FXCollections.observableArrayList(
                "09:00","09:30","10:00","10:30","11:00","11:30",
                "12:00","12:30","13:00","13:30","14:00","14:30",
                "15:00","15:30","16:00","16:30","17:00"
        ));
        loadCandidates();

        // Hide location row initially
        locationField.setVisible(false);
        locationField.setManaged(false);
        locationLabel.setVisible(false);
        locationLabel.setManaged(false);
       // generateMapBtn.setVisible(false);
       // generateMapBtn.setManaged(false);


        // Show/hide based on type selection
        typeComboBox.setOnAction(e -> {
            String selected = typeComboBox.getValue();
            if ("Online".equals(selected)) {
                // Hide location row entirely for online
                locationLabel.setVisible(false);
                locationLabel.setManaged(false);
                locationField.setVisible(false);
                locationField.setManaged(false);
               // searchMapBtn.setVisible(false);
                //searchMapBtn.setManaged(false);
                mapView.setVisible(false);
                mapView.setManaged(false);
                mapLabel.setVisible(false);
                mapLabel.setManaged(false);
                locationField.clear();
            } else if ("In Person".equals(selected)) {
                locationLabel.setText("Address:");
                locationField.setPromptText("Type an address...");
                locationField.clear();
                showLocationRow();
            }
        });
    }

    private void showLocationRow() {
        locationField.setVisible(true);
        locationField.setManaged(true);
        locationLabel.setVisible(true);
        locationLabel.setManaged(true);
    }

    // Called when recruiter clicks 📍 Generate for in-person
    @FXML
    private void handleGenerateMap() {
        String address = locationField.getText().trim();
        if (address.isEmpty()) {
            showAlert("Error", "Please type an address first.");
            return;
        }
        try {
            String encoded = URLEncoder.encode(address, "UTF-8");
            String mapLink = "https://www.openstreetmap.org/search?query=" + encoded;
            locationField.setText(mapLink);
        } catch (UnsupportedEncodingException e) {
            showAlert("Error", "Could not generate map link.");
        }
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

            // Validate link for online
            String locationLink = locationField.getText().trim();
            if ("Online".equals(type) && !locationLink.startsWith("http")) {
                showAlert("Error", "Please paste a valid meeting link (must start with http)."); return;
            }

            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(timeComboBox.getValue()));
            Interview interview = new Interview(
                    selected.getId_candidat(), dateTime, type,
                    jobOfferField.getText(), interviewerField.getText(),
                    locationLink.isEmpty() ? null : locationLink
            );

            interviewService.ajouter(interview);
            showAlert("Success", "Interview added successfully!");
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
    @FXML
    private void handleSearchLocation() {
        String address = locationField.getText().trim();
        if (address.isEmpty()) {
            showAlert("Error", "Please type an address first.");
            return;
        }

        // Run in background so UI doesn't freeze
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

                // Build OpenStreetMap embed URL
                double offset = 0.005;
                String mapUrl = String.format(
                        "https://www.openstreetmap.org/export/embed.html" +
                                "?bbox=%.6f,%.6f,%.6f,%.6f&layer=mapnik&marker=%.6f,%.6f",
                        lon - offset, lat - offset,
                        lon + offset, lat + offset,
                        lat, lon
                );

                // Load map in WebView
                mapView.getEngine().load("about:blank");
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.millis(100)
                );
                pause.setOnFinished(ev -> mapView.getEngine().load(mapUrl));
                pause.play();
                mapView.setVisible(true);
                mapView.setManaged(true);
                mapLabel.setVisible(true);
                mapView.setZoom(1.5);
                mapLabel.setManaged(true);

                // Save the direct link to the location
                String locationLink = String.format(
                        "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=17/%.6f/%.6f",
                        lat, lon, lat, lon
                );
                locationField.setText(locationLink);
            });
        }).start();
    }
}
