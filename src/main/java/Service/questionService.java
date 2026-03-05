package Service;

import Model.question;
import Utils.Mydb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class questionService {

    private final Connection cnx;

    public questionService() {
        cnx = Mydb.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    // Questions are now grouped purely by job_offer_id — no id_skill needed.

    public int addquestion(String statement, BigDecimal points, int jobOfferId) throws SQLException {
        String sql = "INSERT INTO question(statement, points, job_offer_id) VALUES (?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, statement);
        ps.setBigDecimal(2, (points == null) ? new BigDecimal("1.00") : points);
        ps.setInt(3, jobOfferId);
        ps.executeUpdate();

        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    // ── READ by job offer (primary lookup) ────────────────────────────────────

    public List<question> getquestionsByJobOffer(int jobOfferId) throws SQLException {
        List<question> list = new ArrayList<>();
        String sql = "SELECT id_question, job_offer_id, statement, points " +
                "FROM question WHERE job_offer_id=? ORDER BY id_question ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, jobOfferId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            question q = new question();
            q.setIdQuestion(rs.getInt("id_question"));
            q.setJobOfferId(rs.getInt("job_offer_id"));
            q.setStatement(rs.getString("statement"));
            q.setPoints(rs.getBigDecimal("points"));
            list.add(q);
        }
        return list;
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────

    public List<question> getAllquestions() throws SQLException {
        List<question> list = new ArrayList<>();
        String sql = "SELECT id_question, job_offer_id, statement, points " +
                "FROM question ORDER BY id_question DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            question q = new question();
            q.setIdQuestion(rs.getInt("id_question"));
            q.setJobOfferId(rs.getInt("job_offer_id"));
            q.setStatement(rs.getString("statement"));
            q.setPoints(rs.getBigDecimal("points"));
            list.add(q);
        }
        return list;
    }

    // ── READ by ID ────────────────────────────────────────────────────────────

    public question getquestionById(int idquestion) throws SQLException {
        String sql = "SELECT id_question, job_offer_id, statement, points " +
                "FROM question WHERE id_question=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idquestion);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            question q = new question();
            q.setIdQuestion(rs.getInt("id_question"));
            q.setJobOfferId(rs.getInt("job_offer_id"));
            q.setStatement(rs.getString("statement"));
            q.setPoints(rs.getBigDecimal("points"));
            return q;
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public void updatequestion(int idquestion, String statement, BigDecimal points) throws SQLException {
        String sql = "UPDATE question SET statement=?, points=? WHERE id_question=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, statement);
        ps.setBigDecimal(2, (points == null) ? new BigDecimal("1.00") : points);
        ps.setInt(3, idquestion);
        ps.executeUpdate();
    }

    // ── DELETE (cascade removes choices via FK) ───────────────────────────────

    public void deletequestion(int idquestion) throws SQLException {
        String sql = "DELETE FROM question WHERE id_question=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idquestion);
        ps.executeUpdate();
    }

    // ── COUNT per job offer ───────────────────────────────────────────────────

    public int countquestionsByJobOffer(int jobOfferId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM question WHERE job_offer_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, jobOfferId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("cnt");
        return 0;
    }
}