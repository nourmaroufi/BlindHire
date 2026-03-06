package Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Model.Interview;
import Service.InterviewService;
import Service.NominatimService;
import Service.userservice;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.scene.web.WebView;

/**
 * Controller for addInterview.fxml
 *
 * Flow:
 *   InterviewPage.buildScoreCard() calls schedBtn.setOnAction(...)
 *     → openAddInterview(idScore, idUser, jobTitle, jobId)
 *     → loads this controller and calls prefillFromScore(idScore, idUser, jobTitle)
 *
 * What this controller needs to insert into `interview`:
 *   date            ← picked by recruiter
 *   type            ← picked by recruiter  ("Online" | "In Person")
 *   location_link   ← picked by recruiter  (null if Online)
 *   id_score        ← passed in from score row  ← THE KEY FK
 *   interviewer_id  ← current logged-in recruiter's id
 */
public class AddInterviewController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private Label    headerTitleLabel;
    @FXML private Label    headerSubLabel;
    @FXML private Label    jobOfferLabel;
    @FXML private Label    candidateLabel;
    @FXML private Label    scoreIdLabel;

    @FXML private DatePicker         datePicker;
    @FXML private ComboBox<String>   timeComboBox;
    @FXML private ComboBox<String>   typeComboBox;
    @FXML private TextField          interviewerField;

    @FXML private Label    locationLabel;
    @FXML private TextField locationField;
    @FXML private Button   searchMapBtn;
    @FXML private WebView  mapView;
    @FXML private Label    mapLabel;

    // ── State passed in from InterviewPage ────────────────────────────────────
    private long   prefillIdScore;
    private long   prefillCandidateId;
    private String prefillJobTitle;

    // ── Services ──────────────────────────────────────────────────────────────
    private final InterviewService  interviewService  = new InterviewService();
    private final NominatimService  nominatimService  = new NominatimService();
    private final userservice       userService       = new userservice();

    @FXML
    public void initialize() {
        // Time slots
        timeComboBox.setItems(FXCollections.observableArrayList(
                "09:00","09:30","10:00","10:30","11:00","11:30",
                "12:00","12:30","13:00","13:30","14:00","14:30",
                "15:00","15:30","16:00","16:30","17:00"
        ));

        // Interview type
        typeComboBox.setItems(FXCollections.observableArrayList("Online", "In Person"));

        // Block past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
                if (date.isBefore(LocalDate.now()))
                    setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: #334155;");
            }
        });

        // Hide location row until "In Person" is selected
        hideLocationRow();

        typeComboBox.setOnAction(e -> {
            if ("Online".equals(typeComboBox.getValue())) {
                hideLocationRow();
                locationField.clear();
            } else if ("In Person".equals(typeComboBox.getValue())) {
                showLocationRow();
            }
        });
    }

    /**
     * Called by InterviewPage immediately after loading the FXML.
     * Populates read-only info pills and stores the FK values used on submit.
     *
     * @param idScore           score.id_score  — FK for interview.id_score
     * @param candidateId       score.id_user   — stored for reference
     * @param candidateUsername user.username (or nom+prenom) — displayed in UI
     * @param jobTitle          job_offer.title — displayed in UI
     */
    public void prefillFromScore(long idScore, long candidateId, String candidateUsername, String jobTitle) {
        this.prefillIdScore      = idScore;
        this.prefillCandidateId  = candidateId;
        this.prefillJobTitle     = jobTitle;

        // Update header
        headerTitleLabel.setText("Schedule Interview — " + jobTitle);
        headerSubLabel.setText("Booking for " + candidateUsername);

        // Update info pills
        jobOfferLabel.setText(jobTitle);
        candidateLabel.setText(candidateUsername);   // ← username instead of #id
        scoreIdLabel.setText("Score #" + idScore);

        // Pre-fill interviewer with the logged-in recruiter's name
        Model.User current = userService.getCurrentUser();
        if (current != null) {
            interviewerField.setText(current.getNom() + " " + current.getPrenom());
        }
    }

    // ── Location visibility ───────────────────────────────────────────────────

    private void hideLocationRow() {
        setLocationRowVisible(false);
        setMapVisible(false);
    }

    private void showLocationRow() {
        setLocationRowVisible(true);
        // Map stays hidden until a search is performed
    }

    private void setLocationRowVisible(boolean v) {
        locationLabel.setVisible(v);   locationLabel.setManaged(v);
        locationField.setVisible(v);   locationField.setManaged(v);
        searchMapBtn.setVisible(v);    searchMapBtn.setManaged(v);
    }

    private void setMapVisible(boolean v) {
        mapView.setVisible(v);  mapView.setManaged(v);
        mapLabel.setVisible(v); mapLabel.setManaged(v);
    }

    // ── Map search ────────────────────────────────────────────────────────────

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

                double lat = coords[0], lon = coords[1], offset = 0.005;
                String mapUrl = String.format(
                        "https://www.openstreetmap.org/export/embed.html" +
                                "?bbox=%.6f,%.6f,%.6f,%.6f&layer=mapnik&marker=%.6f,%.6f",
                        lon - offset, lat - offset,
                        lon + offset, lat + offset,
                        lat, lon
                );
                mapView.getEngine().load(mapUrl);
                setMapVisible(true);

                // Store the OSM permalink in the field so it's saved to DB
                String locationLink = String.format(
                        "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=17/%.6f/%.6f",
                        lat, lon, lat, lon
                );
                locationField.setText(locationLink);
            });
        }).start();
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @FXML
    private void handleAddInterview() {
        // ── Validate required fields ──────────────────────────────────────────
        if (datePicker.getValue() == null) {
            showAlert("Validation", "Please select a date."); return;
        }
        if (timeComboBox.getValue() == null) {
            showAlert("Validation", "Please select a time."); return;
        }
        if (typeComboBox.getValue() == null) {
            showAlert("Validation", "Please select an interview type."); return;
        }

        String type         = typeComboBox.getValue();
        String locationLink;

        if ("In Person".equals(type)) {
            locationLink = locationField.getText().trim();
            if (locationLink.isEmpty()) {
                showAlert("Validation",
                        "Please enter and search an address for an in-person interview.");
                return;
            }
        } else {
            // Online interview — pre-generate the Jitsi room URL.
            // We use a temporary placeholder based on score ID since we don't have
            // the interview DB id yet. The recruiter's "Start Call" button in
            // InterviewPage always uses the real id after insertion.
            locationLink = "https://meet.jit.si/blindhire-score-" + prefillIdScore;
        }

        // ── Build datetime ────────────────────────────────────────────────────
        LocalDateTime dateTime = LocalDateTime.of(
                datePicker.getValue(),
                LocalTime.parse(timeComboBox.getValue())
        );

        // ── Interviewer id = current logged-in recruiter ──────────────────────
        Model.User current     = userService.getCurrentUser();
        long       interviewerId = current != null ? current.getId() : 0L;

        // ── Insert using the FK id_score passed in from the score row ─────────
        Interview interview = new Interview(
                dateTime,
                type,
                locationLink,
                prefillIdScore,    // ← FK to score table
                interviewerId      // ← FK to user table (recruiter)
        );

        try {
            interviewService.ajouter(interview);
            showAlert("Success", "Interview scheduled successfully!");
            closeWindow();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not save the interview:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @FXML
    private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}