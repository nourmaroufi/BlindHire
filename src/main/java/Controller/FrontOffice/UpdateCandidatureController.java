package Controller.FrontOffice;

import Service.CandidatureService;
import Model.Candidature;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class UpdateCandidatureController {

    @FXML private TextField  salaryField;
    @FXML private TextArea   coverLetterArea;

    // hidden field — stores the full path in memory
    @FXML private TextField  cvPathField;

    // visible label — shows filename or "current CV will be kept"
    @FXML private Label      cvFileNameLabel;

    // read-only job title label
    @FXML private Label      jobTitleLabel;

    @FXML private Button     browseBtn;

    private Candidature       candidature;
    private CandidatureService candidatureService;

    // ── true only if the user picked a new file this session ─────────────────
    private boolean newCvSelected = false;

    @FXML
    public void initialize() {
        candidatureService = new CandidatureService();

        // Hover effects on Browse button
        if (browseBtn != null) {
            String normal = "-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-border-color: #4f46e5; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 20; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
            String hover  = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-border-color: #4f46e5; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 20; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
            browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(hover));
            browseBtn.setOnMouseExited(e  -> browseBtn.setStyle(normal));
        }
    }

    /**
     * Pre-fills all fields from the existing candidature.
     * Called by MyApplicationsController right after FXMLLoader.load().
     */
    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;

        // Salary — show blank if 0/null
        if (candidature.getExpectedSalary() != null && candidature.getExpectedSalary() > 0) {
            salaryField.setText(String.valueOf(candidature.getExpectedSalary().intValue()));
        }

        // Cover letter
        if (candidature.getCoverLetter() != null)
            coverLetterArea.setText(candidature.getCoverLetter());

        // CV path — store internally but show only the filename to the user
        if (candidature.getCvPath() != null && !candidature.getCvPath().isBlank()) {
            cvPathField.setText(candidature.getCvPath());
            // show just the filename, not the full path
            String filename = new File(candidature.getCvPath()).getName();
            cvFileNameLabel.setText("Current CV: " + filename);
            cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        }

        // Resolve job title
        if (jobTitleLabel != null) {
            try {
                String title = new CandidatureService()
                        .getJobTitleById(candidature.getJobOfferId());
                jobTitleLabel.setText(title != null ? title : "—");
            } catch (Exception ignored) {}
        }
    }

    /** Opens a FileChooser for PDF / Word documents. */
    @FXML
    private void handleBrowseCV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select your CV");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("PDF Files",  "*.pdf"),
                new FileChooser.ExtensionFilter("Word Files", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("All Files",  "*.*")
        );

        // Pre-open the folder of the existing CV if available
        if (candidature != null && candidature.getCvPath() != null) {
            File existing = new File(candidature.getCvPath()).getParentFile();
            if (existing != null && existing.exists())
                chooser.setInitialDirectory(existing);
        }

        Stage stage = (Stage) salaryField.getScene().getWindow();
        File chosen = chooser.showOpenDialog(stage);

        if (chosen != null) {
            cvPathField.setText(chosen.getAbsolutePath());
            cvFileNameLabel.setText("✅  " + chosen.getName());
            cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669; -fx-font-weight: bold;");
            newCvSelected = true;
        }
        // If user cancelled the dialog → do nothing, existing path is preserved
    }

    @FXML
    private void handleLoadCvFromProfile() {
        Service.userservice us = new Service.userservice();
        Model.User currentUser = us.getCurrentUser();
        if (currentUser == null) {
            cvFileNameLabel.setText("⚠️  No active session found");
            cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            return;
        }
        try {
            // Fetch the most recent CV from the user's other applications (excluding current one)
            java.util.List<Model.Candidature> past =
                    candidatureService.getCandidaturesByUserId(currentUser.getId());

            String profileCv = past.stream()
                    .filter(c -> c.getCvPath() != null && !c.getCvPath().isBlank())
                    .findFirst()  // already ordered by date DESC
                    .map(Model.Candidature::getCvPath)
                    .orElse(null);

            if (profileCv == null) {
                cvFileNameLabel.setText("⚠️  No previous CV found in your applications");
                cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                return;
            }
            File cvFile = new File(profileCv);
            if (!cvFile.exists()) {
                cvFileNameLabel.setText("⚠️  Previous CV file not found on disk");
                cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                return;
            }
            cvPathField.setText(cvFile.getAbsolutePath());
            cvFileNameLabel.setText("✅  " + cvFile.getName() + " (from profile)");
            cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669; -fx-font-weight: bold;");
            newCvSelected = true;

        } catch (Exception e) {
            e.printStackTrace();
            cvFileNameLabel.setText("⚠️  Error loading CV from profile");
            cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Salary — validate and parse
            String salaryText = salaryField.getText().trim();
            if (!salaryText.isEmpty()) {
                try {
                    candidature.setExpectedSalary(Double.parseDouble(salaryText));
                } catch (NumberFormatException e) {
                    cvFileNameLabel.setText("⚠️  Salary must be a number");
                    cvFileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    return;
                }
            }

            // Cover letter
            candidature.setCoverLetter(coverLetterArea.getText());

            // CV path — only update if user picked a new file
            if (newCvSelected) {
                candidature.setCvPath(cvPathField.getText());
            }
            // else: candidature.cvPath stays exactly as it was → no overwrite

            candidatureService.updateCandidature(candidature);
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) salaryField.getScene().getWindow();
        stage.close();
    }
}