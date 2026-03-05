package Service;

import Model.FavJob;
import Model.JobOffer;
import Utils.Mydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for the fav_job table: fav_job(id, user_id, job_id)
 * Run the SQL once to create the table:
 *   CREATE TABLE IF NOT EXISTS fav_job (
 *       id      INT AUTO_INCREMENT PRIMARY KEY,
 *       user_id INT NOT NULL,
 *       job_id  INT NOT NULL,
 *       saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 *       UNIQUE KEY uq_fav (user_id, job_id)
 *   );
 */
public class FavJobService {

    private final Connection cnx;

    public FavJobService() {
        cnx = Mydb.getInstance().getConnection();
        ensureTableExists();
    }

    // ── table bootstrap ────────────────────────────────────────────────────────

    private void ensureTableExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS fav_job (
                id       INT AUTO_INCREMENT PRIMARY KEY,
                user_id  INT NOT NULL,
                job_id   INT NOT NULL,
                saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uq_fav (user_id, job_id)
            )
            """;
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("[FavJobService] table creation failed: " + e.getMessage());
        }
    }

    // ── public API ─────────────────────────────────────────────────────────────

    /** Save a job to favourites. Returns true if newly added, false if already saved. */
    public boolean save(int userId, int jobId) throws SQLException {
        if (isSaved(userId, jobId)) return false;
        String sql = "INSERT INTO fav_job (user_id, job_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, jobId);
            ps.executeUpdate();
            return true;
        }
    }

    /** Remove a job from favourites. */
    public void remove(int userId, int jobId) throws SQLException {
        String sql = "DELETE FROM fav_job WHERE user_id = ? AND job_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, jobId);
            ps.executeUpdate();
        }
    }

    /** Check whether a specific job is already saved by this user. */
    public boolean isSaved(int userId, int jobId) throws SQLException {
        String sql = "SELECT 1 FROM fav_job WHERE user_id = ? AND job_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Return all favourite JobOffer objects for a user, newest first. */
    public List<JobOffer> getFavourites(int userId) throws SQLException {
        List<JobOffer> list = new ArrayList<>();
        String sql = """
            SELECT jo.*, fj.saved_at
            FROM fav_job fj
            JOIN job_offer jo ON jo.id = fj.job_id
            WHERE fj.user_id = ?
            ORDER BY fj.saved_at DESC
            """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JobOffer jo = new JobOffer();
                    jo.setId(rs.getInt("id"));
                    jo.setTitle(rs.getString("title"));
                    jo.setDescription(rs.getString("description"));
                    jo.setType(rs.getString("type"));
                    jo.setStatus(rs.getString("status"));
                    jo.setRequiredSkills(rs.getString("required_skills"));
                    jo.setRecruiterId(rs.getInt("recruiter_id"));
                    // posting_date
                    java.sql.Date pd = rs.getDate("posting_date");
                    if (pd != null) jo.setPostingDate(pd.toLocalDate());
                    // offered_salary
                    double sal = rs.getDouble("offered_salary");
                    if (!rs.wasNull()) jo.setOfferedSalary(sal);
                    list.add(jo);
                }
            }
        }
        return list;
    }

    /** Count how many jobs are saved by this user. */
    public int countFavourites(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fav_job WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── FavJob model methods ───────────────────────────────────────────────────

    /** Return a FavJob model for a specific user+job pair, or null if not saved. */
    public FavJob getFavJob(int userId, int jobId) throws SQLException {
        String sql = "SELECT id, user_id, job_id, saved_at FROM fav_job WHERE user_id = ? AND job_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Return all FavJob rows for a user as model objects, newest first. */
    public List<FavJob> getFavJobRows(int userId) throws SQLException {
        List<FavJob> list = new ArrayList<>();
        String sql = "SELECT id, user_id, job_id, saved_at FROM fav_job WHERE user_id = ? ORDER BY saved_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Map a ResultSet row → FavJob model. */
    private FavJob mapRow(ResultSet rs) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp("saved_at");
        java.time.LocalDateTime savedAt = ts != null ? ts.toLocalDateTime() : null;
        return new FavJob(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("job_id"),
                savedAt
        );
    }
}