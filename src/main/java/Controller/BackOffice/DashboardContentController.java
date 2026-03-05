package Controller.BackOffice;
import Service.JobOfferService;
import Service.CandidatureService;
import Model.JobOffer;
import Model.Candidature;
import Model.User;
import Service.userservice;
import Utils.Mydb;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label greetingLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label dayNumLabel;
    @FXML private Label monthYearLabel;

    // ── KPI cards ─────────────────────────────────────────────────────────────
    @FXML private Label kpiTotalUsers;
    @FXML private Label kpiTotalJobs;
    @FXML private Label kpiTotalApps;
    @FXML private Label kpiPending;

    // ── User breakdown ────────────────────────────────────────────────────────
    @FXML private Label breakAdmins;
    @FXML private Label breakRecruiters;
    @FXML private Label breakClients;

    // ── Application status ────────────────────────────────────────────────────
    @FXML private Label statAccepted;
    @FXML private Label statPending;
    @FXML private Label statRejected;

    // ── Acceptance rate ───────────────────────────────────────────────────────
    @FXML private Label acceptanceRateLabel;
    @FXML private Label acceptanceSubLabel;

    // ── Glance banner ─────────────────────────────────────────────────────────
    @FXML private Label glanceTotalUsers;
    @FXML private Label glanceTotalJobs;
    @FXML private Label glanceTotalApps;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateHeader();
        populateKpis();
    }

    private void populateHeader() {
        try {
            User current = new userservice().getCurrentUser();
            String name = (current != null && current.getNom() != null) ? current.getNom() : "Admin";
            greetingLabel.setText(timeGreeting() + ", " + name + " 👋");
        } catch (Exception e) {
            greetingLabel.setText(timeGreeting() + ", Admin 👋");
        }
        LocalDate today = LocalDate.now();
        dayNumLabel.setText(String.valueOf(today.getDayOfMonth()));
        monthYearLabel.setText(today.format(
                DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)).toUpperCase());
    }

    private void populateKpis() {

        try {
            userservice userService = new userservice();
            JobOfferService jobService = new JobOfferService();
            CandidatureService candService = new CandidatureService();

            // ── Users ─────────────────────────────────────────────
            int admins     = userService.countByRole("admin");
            int recruiters = userService.countByRole("recruteur");
            int clients    = userService.countByRole("client");
            int totalUsers = admins + recruiters + clients;

            kpiTotalUsers.setText(String.valueOf(totalUsers));
            breakAdmins.setText(String.valueOf(admins));
            breakRecruiters.setText(String.valueOf(recruiters));
            breakClients.setText(String.valueOf(clients));
            glanceTotalUsers.setText(String.valueOf(totalUsers));

            // ── Job Offers ────────────────────────────────────────
            int totalJobs = jobService.getJobCount();
            kpiTotalJobs.setText(String.valueOf(totalJobs));
            glanceTotalJobs.setText(String.valueOf(totalJobs));

            // ── Candidatures ──────────────────────────────────────
            int accepted = candService.countByStatus("accepted");
            int pending  = candService.countByStatus("pending");
            int rejected = candService.countByStatus("rejected");
            int totalApps = accepted + pending + rejected;

            kpiTotalApps.setText(String.valueOf(totalApps));
            kpiPending.setText(String.valueOf(pending));

            statAccepted.setText(String.valueOf(accepted));
            statPending.setText(String.valueOf(pending));
            statRejected.setText(String.valueOf(rejected));

            glanceTotalApps.setText(String.valueOf(totalApps));

            // ── Acceptance Rate ───────────────────────────────────
            if (totalApps > 0) {
                double rate = accepted * 100.0 / totalApps;
                acceptanceRateLabel.setText(String.format("%.0f%%", rate));
                acceptanceSubLabel.setText(
                        accepted + " accepted  ·  " +
                                rejected + " rejected  ·  " +
                                pending + " pending"
                );

                System.out.println(jobService.getJobCount());
                System.out.println(candService.getTotalCandidatures());
            } else {
                acceptanceRateLabel.setText("N/A");
                acceptanceSubLabel.setText("No applications yet");

                System.out.println(jobService.getJobCount());
                System.out.println(candService.getTotalCandidatures());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs a COUNT query and returns the integer result.
     * Prints the exact SQL + full stack trace on failure so you can see
     * precisely which table name or column is wrong.
     */
    private int queryCount(Connection cnx, String sql) {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[Dashboard] Query failed: " + sql);
            e.printStackTrace();
        }
        return 0;
    }

    private String timeGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 18) return "Good afternoon";
        return "Good evening";
    }
}