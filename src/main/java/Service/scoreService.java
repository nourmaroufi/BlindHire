package Service;

import Model.LeaderboardRow;
import Model.score;
import Utils.Mydb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class scoreService {

    private Connection cnx;

    public scoreService() {
        cnx = Mydb.getInstance().getConnection();
    }

    private Connection conn() {
        try { if (cnx == null || cnx.isClosed()) cnx = Mydb.getInstance().getConnection(); }
        catch (SQLException ignored) {}
        return cnx;
    }

    // ── WRITE ─────────────────────────────────────────────────────────────────

    public void addScore(int userId, int jobOfferId, BigDecimal scoreVal) throws SQLException {
        String status = scoreVal.compareTo(new java.math.BigDecimal("50")) >= 0 ? "accepted" : "rejected";
        String sql = "INSERT INTO score(id_user, score, job_offer_id, status) VALUES (?,?,?,?)";
        PreparedStatement ps = conn().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setBigDecimal(2, scoreVal);
        ps.setInt(3, jobOfferId);
        ps.setString(4, status);
        ps.executeUpdate();
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public boolean hasScore(int userId, int jobOfferId) throws SQLException {
        PreparedStatement ps = conn().prepareStatement(
                "SELECT 1 FROM score WHERE id_user=? AND job_offer_id=? LIMIT 1");
        ps.setInt(1, userId);
        ps.setInt(2, jobOfferId);
        return ps.executeQuery().next();
    }

    public List<score> getscoresByUser(int userId) throws SQLException {
        List<score> list = new ArrayList<>();
        PreparedStatement ps = conn().prepareStatement(
                "SELECT id_score, id_user, job_offer_id, score, created_at " +
                        "FROM score WHERE id_user=? ORDER BY created_at DESC");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<score> getscoresByUserAndJobOffer(int userId, int jobOfferId) throws SQLException {
        List<score> list = new ArrayList<>();
        PreparedStatement ps = conn().prepareStatement(
                "SELECT id_score, id_user, job_offer_id, score, created_at " +
                        "FROM score WHERE id_user=? AND job_offer_id=? ORDER BY created_at DESC");
        ps.setInt(1, userId);
        ps.setInt(2, jobOfferId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<LeaderboardRow> getLeaderboardByJobOffer(int jobOfferId) throws SQLException {
        List<LeaderboardRow> list = new ArrayList<>();
        PreparedStatement ps = conn().prepareStatement(
                "SELECT id_user, score FROM score WHERE job_offer_id=? ORDER BY score DESC, id_user ASC");
        ps.setInt(1, jobOfferId);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            list.add(new LeaderboardRow(rs.getInt("id_user"), rs.getBigDecimal("score")));
        return list;
    }

    /** Legacy alias so existing leaderboard panel compiles unchanged. */
    public List<LeaderboardRow> getLeaderboardBySkill(int jobOfferId) throws SQLException {
        return getLeaderboardByJobOffer(jobOfferId);
    }


    /** Returns all scores with status='accepted' for a given job offer, joined with user username. */
    public List<score> getAcceptedByJob(int jobOfferId) throws SQLException {
        List<score> list = new ArrayList<>();
        PreparedStatement ps = conn().prepareStatement(
                "SELECT s.id_score, s.id_user, s.job_offer_id, s.score, s.created_at, s.status, " +
                        "       COALESCE(NULLIF(u.username, ''), CONCAT(u.nom, ' ', u.prenom)) AS candidate_username " +
                        "FROM score s " +
                        "JOIN user u ON s.id_user = u.id " +
                        "WHERE s.job_offer_id=? AND s.status='accepted' ORDER BY s.score DESC");
        ps.setInt(1, jobOfferId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            score s = map(rs);
            s.setCandidateUsername(rs.getString("candidate_username"));
            list.add(s);
        }
        return list;
    }

    /** Counts accepted scores for a given job offer. */
    public int countAcceptedByJob(int jobOfferId) throws SQLException {
        PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM score WHERE job_offer_id=? AND status='accepted'");
        ps.setInt(1, jobOfferId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    private score map(ResultSet rs) throws SQLException {
        score s = new score();
        s.setIdScore(rs.getInt("id_score"));
        s.setIdUser(rs.getInt("id_user"));
        s.setJobOfferId(rs.getInt("job_offer_id"));
        s.setScore(rs.getBigDecimal("score"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        try { s.setStatus(rs.getString("status")); } catch (SQLException ignored) {}
        return s;
    }
}