package Controller;

import Model.JobOffer;
import Model.LeaderboardRow;
import Model.User;
import Service.JobOfferService;
import Service.scoreService;
import Service.userservice;
import ui.LeaderboardPanel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls the two-screen leaderboard:
 *  1. Shows the current recruiter's job offers as clickable cards.
 *  2. On card click, loads ranked quiz scores for that job and shows the leaderboard.
 *
 * Usage (e.g. from DashboardPage):
 *   LeaderboardPanel panel = new LeaderboardPanel();
 *   new LeaderboardController(panel, () -> root.setCenter(prevView));
 *   root.setCenter(panel);
 */
public class LeaderboardController {

    private final LeaderboardPanel view;
    private final Runnable         onBack;
    private final JobOfferService  jobService;
    private final scoreService     scoreService;
    private final userservice      userService;

    public LeaderboardController(LeaderboardPanel view, Runnable onBack) {
        this.view         = view;
        this.onBack       = onBack;
        this.jobService   = new JobOfferService();
        this.scoreService = new scoreService();
        this.userService  = new userservice();

        init();
    }

    private void init() {
        // Back to previous screen (Dashboard)
        view.btnBack.setOnAction(e -> onBack.run());

        // When a job card is clicked, load that job's leaderboard
        view.onJobSelected = this::loadLeaderboardForSelected;

        // Load the recruiter's own jobs
        loadJobs();
    }

    // ── Screen 1: load this recruiter's jobs ─────────────────────────────────

    private void loadJobs() {
        try {
            User current = userService.getCurrentUser();
            List<JobOffer> jobs;

            if (current == null) {
                view.lblStatus.setText("Not logged in");
                view.setJobOffers(List.of());
                return;
            }

            // Admin sees all jobs; recruiter sees only their own
            String role = current.getRole() != null ? current.getRole().name().toLowerCase() : "";
            if (role.equals("admin")) {
                jobs = jobService.getJobOffers();
            } else {
                jobs = jobService.getJobOffersByRecruiterId(current.getId());
            }

            view.setJobOffers(jobs);

        } catch (SQLException e) {
            view.lblStatus.setText("DB error loading jobs");
            e.printStackTrace();
        }
    }

    // ── Screen 2: load ranked scores for the selected job ────────────────────

    private void loadLeaderboardForSelected() {
        JobOffer job = view.selectedJob;
        if (job == null) return;

        try {
            // Rows already sorted DESC by score from the service
            List<LeaderboardRow> rows = scoreService.getLeaderboardByJobOffer(job.getId());

            // Resolve user IDs → full names
            Map<Integer, String> names = resolveNames(rows);

            view.populateLeaderboard(job, rows, names);

        } catch (SQLException e) {
            e.printStackTrace();
            view.lblStatus.setText("DB error loading leaderboard");
        }
    }

    /** Bulk-resolve user IDs to "Prenom Nom" strings. */
    private Map<Integer, String> resolveNames(List<LeaderboardRow> rows) {
        Map<Integer, String> map = new HashMap<>();
        for (LeaderboardRow row : rows) {
            int uid = row.getUserId();
            if (map.containsKey(uid)) continue;
            try {
                User u = new Service.CandidatureService().getCandidateUserById(uid);
                if (u != null) {
                    String n = ((u.getPrenom() != null ? u.getPrenom() : "") + " " +
                            (u.getNom()    != null ? u.getNom()    : "")).trim();
                    map.put(uid, n.isEmpty() ? "User #" + uid : n);
                } else {
                    map.put(uid, "User #" + uid);
                }
            } catch (SQLException e) {
                map.put(uid, "User #" + uid);
            }
        }
        return map;
    }
}