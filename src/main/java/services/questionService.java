package services;

import models.question;
import utils.mydb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class questionService {

    private final Connection cnx;

    public questionService() {
        cnx = mydb.getInstance().getConnection();
    }

    // CREATE
    public int addquestion(int idSkill, String statement, BigDecimal points) throws SQLException {
        String sql = "INSERT INTO question(id_skill, statement, points) VALUES (?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, idSkill);
        ps.setString(2, statement);
        ps.setBigDecimal(3, (points == null) ? new BigDecimal("1.00") : points);
        ps.executeUpdate();

        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    // READ ALL
    public List<question> getAllquestions() throws SQLException {
        List<question> list = new ArrayList<>();
        String sql = "SELECT id_question, id_skill, statement, points FROM question ORDER BY id_question DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            question q = new question();
            q.setIdQuestion(rs.getInt("id_question"));
            q.setIdSkill(rs.getInt("id_skill"));
            q.setStatement(rs.getString("statement"));
            q.setPoints(rs.getBigDecimal("points"));
            list.add(q);
        }
        return list;
    }

    // READ by ID
    public question getquestionById(int idquestion) throws SQLException {
        String sql = "SELECT id_question, id_skill, statement, points FROM question WHERE id_question=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idquestion);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            question q = new question();
            q.setIdQuestion(rs.getInt("id_question"));
            q.setIdSkill(rs.getInt("id_skill"));
            q.setStatement(rs.getString("statement"));
            q.setPoints(rs.getBigDecimal("points"));
            return q;
        }
        return null;
    }

    // READ by Skill (quiz = skill)
    public List<question> getquestionsBySkill(int idSkill) throws SQLException {
        List<question> list = new ArrayList<>();
        String sql = "SELECT id_question, id_skill, statement, points " +
                "FROM question WHERE id_skill=? ORDER BY id_question DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idSkill);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            question q = new question();
            q.setIdQuestion(rs.getInt("id_question"));
            q.setIdSkill(rs.getInt("id_skill"));
            q.setStatement(rs.getString("statement"));
            q.setPoints(rs.getBigDecimal("points"));
            list.add(q);
        }
        return list;
    }

    // UPDATE
    public void updatequestion(int idquestion, int idSkill, String statement, BigDecimal points) throws SQLException {
        String sql = "UPDATE question SET id_skill=?, statement=?, points=? WHERE id_question=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idSkill);
        ps.setString(2, statement);
        ps.setBigDecimal(3, (points == null) ? new BigDecimal("1.00") : points);
        ps.setInt(4, idquestion);
        ps.executeUpdate();
    }

    // DELETE (choices will be deleted automatically because of FK cascade on choice_qcm)
    public void deletequestion(int idquestion) throws SQLException {
        String sql = "DELETE FROM question WHERE id_question=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idquestion);
        ps.executeUpdate();
    }

    // Optional: quick count (useful for UI)
    public int countquestionsBySkill(int idSkill) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM question WHERE id_skill=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idSkill);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("cnt");
        return 0;
    }
}
