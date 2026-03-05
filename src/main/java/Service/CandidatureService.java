package Service;

import Model.Candidature;
import Model.User;
import Model.Role;
import Utils.Mydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CandidatureService — fully migrated to the unified `user` table.
 *
 * Removed:
 *   - getAllCandidats()    (queried the dropped `candidate` table)
 *   - getCandidatById()   (queried the dropped `candidate` table)
 *
 * Added:
 *   - getCandidateUserById()     — looks up a candidate in `user`
 *   - getCandidaturesByUserId()  — "My Applications" query filtered by user.id
 */
public class CandidatureService {

    private final Connection cnx;

    public CandidatureService() {
        this.cnx = Mydb.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Insert a new candidature.
     * candidature.getCandidateId() MUST equal the logged-in User's id
     * (set by AddCandidatureController before calling this).
     */

    /**
     * Accepts one candidature and automatically rejects all others for the same job.
     */
    public void acceptAndRejectOthers(int acceptedCandidatureId, int jobId) throws SQLException {
        // Accept the chosen one
        PreparedStatement accept = cnx.prepareStatement(
                "UPDATE candidature SET status='accepted' WHERE id=?");
        accept.setInt(1, acceptedCandidatureId);
        accept.executeUpdate();

        // Reject all others for the same job
        PreparedStatement reject = cnx.prepareStatement(
                "UPDATE candidature SET status='rejected', rejection_reason='Another candidate was selected for this position.' " +
                        "WHERE job_id=? AND id != ? AND status != 'rejected'");
        reject.setInt(1, jobId);
        reject.setInt(2, acceptedCandidatureId);
        reject.executeUpdate();
    }
    public void addCandidature(Candidature candidature) throws SQLException {
        String sql =
                "INSERT INTO candidature " +
                        "(candidate_id, job_id, application_date, status, " +
                        " cover_letter, cv_path, portfolio_url, expected_salary) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, candidature.getCandidateId());          // ← user.id
        ps.setInt(2, candidature.getJobOfferId());
        ps.setDate(3, candidature.getApplicationDate() != null
                ? Date.valueOf(candidature.getApplicationDate()) : null);
        ps.setString(4, candidature.getStatus() != null
                ? candidature.getStatus() : "pending");
        ps.setString(5, candidature.getCoverLetter());
        ps.setString(6, candidature.getCvPath());
        ps.setString(7, candidature.getPortfolioUrl());
        ps.setDouble(8, candidature.getExpectedSalary() != null
                ? candidature.getExpectedSalary() : 0);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) candidature.setId(rs.getInt(1));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** All candidatures — used by the back-office. */
    public List<Candidature> getAllCandidatures() throws SQLException {
        List<Candidature> list = new ArrayList<>();
        ResultSet rs = cnx.createStatement()
                .executeQuery("SELECT * FROM candidature ORDER BY application_date DESC");
        while (rs.next()) list.add(extract(rs));
        return list;
    }

    /** Candidatures for one specific job offer. */
    public List<Candidature> getCandidaturesByJobOfferId(int jobOfferId) throws SQLException {
        List<Candidature> list = new ArrayList<>();
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM candidature WHERE job_id = ?");
        ps.setInt(1, jobOfferId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(extract(rs));
        return list;
    }

    public int getTotalCandidatures() throws SQLException {
        String sql = "SELECT COUNT(*) FROM candidature";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }
    /**
     * Candidatures submitted by one specific user (front-office "My Applications").
     * Replaces the old getAllCandidatures() call in MyApplicationsController.
     */
    public List<Candidature> getCandidaturesByUserId(int userId) throws SQLException {
        List<Candidature> list = new ArrayList<>();
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM candidature WHERE candidate_id = ? ORDER BY application_date DESC");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(extract(rs));
        return list;
    }

    /**
     * Look up the User who submitted a candidature.
     * Replaces the old getCandidatById() which queried the dropped `candidate` table.
     */
    public User getCandidateUserById(int userId) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT id, nom, prenom, mail, role, skills, experience " +
                        "FROM user WHERE id = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("mail"));
            u.setSkills(rs.getString("skills"));
            u.setExperience(rs.getString("experience"));
            try { u.setRole(Role.valueOf(rs.getString("role"))); }
            catch (Exception ignored) {}
            return u;
        }
        return null;
    }

    /** Fetch a job title from job_offer by id. */
    public String getJobTitleById(int jobId) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT title FROM job_offer WHERE id = ?");
        ps.setInt(1, jobId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString("title") : "Unknown Job";
    }


    /**
     * Returns true if the user has already submitted an application for this job.
     * Used to block duplicate applications.
     */
    public boolean hasApplied(int userId, int jobId) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM candidature WHERE candidate_id = ? AND job_id = ?");
        ps.setInt(1, userId);
        ps.setInt(2, jobId);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    // ── UPDATE / DELETE ───────────────────────────────────────────────────────

    public void updateCandidature(Candidature c) throws SQLException {
        String sql =
                "UPDATE candidature SET status=?, cover_letter=?, cv_path=?, " +
                        "portfolio_url=?, expected_salary=?, rejection_reason=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, c.getStatus());
        ps.setString(2, c.getCoverLetter());
        ps.setString(3, c.getCvPath());
        ps.setString(4, c.getPortfolioUrl());
        ps.setDouble(5, c.getExpectedSalary() != null ? c.getExpectedSalary() : 0);
        ps.setString(6, c.getRejectionReason());
        ps.setInt(7, c.getId());
        ps.executeUpdate();
    }

    public void deleteCandidature(int id) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "DELETE FROM candidature WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    public int countByStatus(String status) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM candidature WHERE status=?");
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    // ── PRIVATE ───────────────────────────────────────────────────────────────

    private Candidature extract(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        c.setCandidateId(rs.getInt("candidate_id"));
        c.setJobOfferId(rs.getInt("job_id"));
        Date d = rs.getDate("application_date");
        if (d != null) c.setApplicationDate(d.toLocalDate());
        c.setStatus(rs.getString("status"));
        c.setCoverLetter(rs.getString("cover_letter"));
        c.setCvPath(rs.getString("cv_path"));
        c.setPortfolioUrl(rs.getString("portfolio_url"));
        c.setExpectedSalary(rs.getDouble("expected_salary"));
        c.setRejectionReason(rs.getString("rejection_reason"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) c.setUpdatedAt(ua.toLocalDateTime());
        return c;
    }
}