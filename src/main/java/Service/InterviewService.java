package Service;

import Model.Interview;
import Utils.Mydb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterviewService {

    Connection conn = Mydb.getInstance().getConnection();

    // ── CREATE ────────────────────────────────────────────────────────────────
    public void ajouter(Interview interview) throws SQLException {
        String sql = "INSERT INTO interview (date, type, location_link, id_score, interviewer_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, Timestamp.valueOf(interview.getDate()));
        ps.setString(2, interview.getType());
        ps.setString(3, interview.getLocationLink());
        ps.setLong(4, interview.getIdScore());
        ps.setLong(5, interview.getInterviewerId());
        ps.executeUpdate();
    }

    // ── READ ALL (for back-office) ────────────────────────────────────────────
    public List<Interview> afficherAll() throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, jo.title AS job_title " +
                "FROM interview i " +
                "LEFT JOIN score s ON i.id_score = s.id_score " +
                "LEFT JOIN job_offer jo ON s.job_offer_id = jo.id";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Interview i = mapRow(rs);
            i.setJob_offer(rs.getString("job_title"));
            list.add(i);
        }
        return list;
    }

    // ── READ BY CANDIDATE (for front-office) ─────────────────────────────────
    // Joins through score to find interviews belonging to a candidate (id_user)
    public List<Interview> afficherByCandidat(int candidatId) throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, jo.title AS job_title " +
                "FROM interview i " +
                "JOIN score s ON i.id_score = s.id_score " +
                "LEFT JOIN job_offer jo ON s.job_offer_id = jo.id " +
                "WHERE s.id_user = ? " +
                "ORDER BY i.date ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, candidatId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Interview i = mapRow(rs);
            i.setJob_offer(rs.getString("job_title"));
            list.add(i);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void update(int id, LocalDateTime dateTime, String type,
                       String locationLink, long idScore, long interviewerId) throws SQLException {
        String sql = "UPDATE interview SET date=?, type=?, location_link=?, " +
                "id_score=?, interviewer_id=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, Timestamp.valueOf(dateTime));
        ps.setString(2, type);
        ps.setString(3, locationLink);
        ps.setLong(4, idScore);
        ps.setLong(5, interviewerId);
        ps.setInt(6, id);
        ps.executeUpdate();
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE interview SET status = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public void updateNotes(int id, String notes) throws SQLException {
        String sql = "UPDATE interview SET notes = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, notes);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    // ── CHECK ─────────────────────────────────────────────────────────────────
    /** Returns true if an interview already exists for the given score (id_score FK). */
    public boolean existsByScore(int idScore) throws SQLException {
        String sql = "SELECT 1 FROM interview WHERE id_score = ? LIMIT 1";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idScore);
        return ps.executeQuery().next();
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM interview WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ── HELPER ────────────────────────────────────────────────────────────────
    private Interview mapRow(ResultSet rs) throws SQLException {
        return new Interview(
                rs.getInt("id"),
                rs.getTimestamp("date").toLocalDateTime(),
                rs.getString("type"),
                rs.getString("location_link"),
                rs.getString("status"),
                rs.getString("notes"),
                rs.getLong("id_score"),
                rs.getLong("interviewer_id")
        );
    }
}