package Controller;

import Model.Candidature;
import Model.JobOffer;
import Model.User;
import Model.score;
import Service.CandidatureService;
import Service.JobOfferService;
import Service.scoreService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MyQuizzesController
 *
 * For each candidature the user has ever submitted it assembles one QuizRow:
 *
 *   candidature  →  status (pending / accepted / rejected)
 *   job_offer    →  title, required_skills
 *   score        →  quiz taken?, score %, pass/fail  (pass = score >= 50)
 *
 * NOTE: scoreService.getscoresByUserAndJobOffer() selects only
 *       id_score, id_user, job_offer_id, score, created_at  (no status column).
 *       Pass/fail is derived here from the raw percentage, matching the
 *       same threshold used in QuizResultPopup and scoreService.addScore().
 */
public class MyQuizzesController {

    private final CandidatureService candidatureService = new CandidatureService();
    private final JobOfferService    jobOfferService    = new JobOfferService();
    private final scoreService       scoreService       = new scoreService();

    // ── Row record handed to the panel ────────────────────────────────────────

    public record QuizRow(
            int        jobOfferId,
            String     jobTitle,
            String     requiredSkills,

            /** "pending" | "accepted" | "rejected" */

            /** false  → user never submitted this quiz */
            boolean    quizTaken,

            /** null when not taken */
            BigDecimal scorePercent,

            /** true = score >= 50, false = score < 50, null = not taken */
            Boolean    quizPassed
    ) {
        // ── Human-readable helpers used directly by the panel ─────────────────



        /** "73%" or "—" */
        public String scoreLabel() {
            if (scorePercent == null) return "—";
            return scorePercent.stripTrailingZeros().toPlainString() + "%";
        }

        /** "🏆  Passed" | "❌  Failed" | "—" */
        public String resultLabel() {
            if (!quizTaken || quizPassed == null) return "—";
            return quizPassed ? "🏆  Accepted" : "❌  Failed";
        }
    }

    // ── Main data load ────────────────────────────────────────────────────────

    /**
     * Returns one QuizRow per candidature the user submitted.
     * All exceptions are caught so the panel always renders something.
     */
    public List<QuizRow> loadRows(User user) {
        List<QuizRow> rows = new ArrayList<>();
        if (user == null) return rows;

        List<Candidature> candidatures;
        try {
            candidatures = candidatureService.getCandidaturesByUserId(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            return rows;
        }

        for (Candidature c : candidatures) {
            // Only show accepted applications — pending/rejected are excluded
            if (!"accepted".equalsIgnoreCase(c.getStatus())) continue;
            int jobId = c.getJobOfferId();

            // ── 1. Job offer metadata ──────────────────────────────────────────
            String jobTitle       = "Job #" + jobId;
            String requiredSkills = "";
            try {
                JobOffer job = jobOfferService.getJobOfferById(jobId);
                if (job != null) {
                    if (job.getTitle() != null && !job.getTitle().isBlank())
                        jobTitle = job.getTitle();
                    if (job.getRequiredSkills() != null)
                        requiredSkills = job.getRequiredSkills();
                }
            } catch (SQLException ignored) {}

            // ── 2. Quiz / score data ──────────────────────────────────────────
            boolean    quizTaken    = false;
            BigDecimal scorePercent = null;
            Boolean    quizPassed   = null;

            try {
                // Returns DESC by created_at → index 0 = most recent attempt
                List<score> scores =
                        scoreService.getscoresByUserAndJobOffer(user.getId(), jobId);

                if (!scores.isEmpty()) {
                    score latest   = scores.get(0);
                    quizTaken      = true;
                    scorePercent   = latest.getScore();

                    // Derive pass/fail from the raw score (same rule as scoreService.addScore)
                    if (scorePercent != null) {
                        quizPassed = scorePercent.compareTo(new BigDecimal("50")) >= 0;
                    }
                }
            } catch (SQLException ignored) {}

            rows.add(new QuizRow(
                    jobId,
                    jobTitle,
                    requiredSkills,
                    quizTaken,
                    scorePercent,
                    quizPassed
            ));
        }

        return rows;
    }
}