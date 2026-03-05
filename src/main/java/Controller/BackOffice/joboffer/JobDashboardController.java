package Controller.BackOffice.joboffer;

import Service.CandidatureService;
import Service.JobOfferService;
import Model.JobOffer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class JobDashboardController {

    // ── KPI cards ─────────────────────────────────────────────────────────────
    @FXML private Label totalJobsLabel;
    @FXML private Label activeJobsLabel;
    @FXML private Label totalAppsLabel;
    @FXML private Label pendingAppsLabel;

    // ── Job status breakdown ──────────────────────────────────────────────────
    @FXML private Label jobStatusActiveLabel;
    @FXML private Label jobStatusPendingLabel;
    @FXML private Label jobStatusClosedLabel;

    // ── Application status ────────────────────────────────────────────────────
    @FXML private Label appAcceptedLabel;
    @FXML private Label appPendingLabel;
    @FXML private Label appRejectedLabel;

    // ── Acceptance rate ───────────────────────────────────────────────────────
    @FXML private Label acceptanceRateLabel;
    @FXML private Label acceptanceSubLabel;

    // ── Glance banner ─────────────────────────────────────────────────────────
    @FXML private Label glanceTotalJobs;
    @FXML private Label glanceTotalApps;
    @FXML private Label glanceAcceptanceRate;

    // ── Date header ───────────────────────────────────────────────────────────
    @FXML private Label dayNumLabel;
    @FXML private Label monthYearLabel;

    private final JobOfferService    jobSvc = new JobOfferService();
    private final CandidatureService appSvc = new CandidatureService();

    @FXML
    public void initialize() {
        populateDate();
        loadStats();
    }

    private void populateDate() {
        LocalDate today = LocalDate.now();
        dayNumLabel.setText(String.valueOf(today.getDayOfMonth()));
        monthYearLabel.setText(today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)).toUpperCase());
    }

    private void loadStats() {
        // ── Job offer counts ──────────────────────────────────────────────────
        int totalJobs   = safe(jobSvc::getJobCount);
        int activeJobs  = safeJobStatus("active");
        int pendingJobs = safeJobStatus("pending");
        int closedJobs  = safeJobStatus("closed");

        totalJobsLabel.setText(String.valueOf(totalJobs));
        activeJobsLabel.setText(String.valueOf(activeJobs));
        jobStatusActiveLabel.setText(String.valueOf(activeJobs));
        jobStatusPendingLabel.setText(String.valueOf(pendingJobs));
        jobStatusClosedLabel.setText(String.valueOf(closedJobs));

        glanceTotalJobs.setText(String.valueOf(totalJobs));

        // ── Application counts ────────────────────────────────────────────────
        int accepted  = safeAppStatus("accepted");
        int pending   = safeAppStatus("pending");
        int rejected  = safeAppStatus("rejected");
        int totalApps = accepted + pending + rejected;

        totalAppsLabel.setText(String.valueOf(totalApps));
        pendingAppsLabel.setText(String.valueOf(pending));

        appAcceptedLabel.setText(String.valueOf(accepted));
        appPendingLabel.setText(String.valueOf(pending));
        appRejectedLabel.setText(String.valueOf(rejected));

        glanceTotalApps.setText(String.valueOf(totalApps));

        // ── Acceptance rate ───────────────────────────────────────────────────
        if (totalApps > 0) {
            double rate = accepted * 100.0 / totalApps;
            String rateStr = String.format("%.0f%%", rate);
            acceptanceRateLabel.setText(rateStr);
            acceptanceSubLabel.setText(accepted + " accepted  ·  " + rejected + " rejected  ·  " + pending + " pending");
            glanceAcceptanceRate.setText(rateStr);
        } else {
            acceptanceRateLabel.setText("N/A");
            acceptanceSubLabel.setText("No applications yet");
            glanceAcceptanceRate.setText("N/A");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface IntSupplier { int get(); }

    private int safe(IntSupplier s) {
        try { return s.get(); } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    /** Count job offers by status from the full list. */
    private int safeJobStatus(String status) {
        try {
            List<JobOffer> all = jobSvc.getJobOffers();
            return (int) all.stream()
                    .filter(j -> status.equalsIgnoreCase(j.getStatus()))
                    .count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Count candidatures by status. */
    private int safeAppStatus(String status) {
        try { return appSvc.countByStatus(status); }
        catch (Exception e) { e.printStackTrace(); return 0; }
    }
}