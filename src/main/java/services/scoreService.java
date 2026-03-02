package services;

import models.score;
import utils.mydb;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class scoreService {

    private final Connection cnx;

    public scoreService() {
        cnx = mydb.getInstance().getConnection();
    }


    // WRITE: save a score (history version table)
    public void addScore(int userId, int skillId, BigDecimal score) throws SQLException {
        String sql = "INSERT INTO score(id_user, id_skill, score) VALUES (?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, skillId);
        ps.setBigDecimal(3, score);
        ps.executeUpdate();
    }

    // LIST: all scores for a user (latest first)
    public List<score> getscoresByUser(int userId) throws SQLException {
        List<score> list = new ArrayList<>();
        String sql = "SELECT id_score, id_user, id_skill, score, created_at " +
                "FROM score WHERE id_user=? ORDER BY created_at DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            score s = new score();
            s.setIdScore(rs.getInt("id_score"));
            s.setIdUser(rs.getInt("id_user"));
            s.setIdSkill(rs.getInt("id_skill"));
            s.setScore(rs.getBigDecimal("score"));
            s.setCreatedAt(rs.getTimestamp("created_at"));
            list.add(s);
        }
        return list;
    }
    public List<models.LeaderboardRow> getLeaderboardBySkill(int idSkill) throws SQLException {
        List<models.LeaderboardRow> list = new ArrayList<>();
        String sql = "SELECT id_user, score FROM score WHERE id_skill=? ORDER BY score DESC, id_user ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idSkill);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new models.LeaderboardRow(
                    rs.getInt("id_user"),
                    rs.getBigDecimal("score")
            ));
        }
        return list;
    }
    // LIST: history for a user + skill
    public List<score> getscoresByUserAndSkill(int userId, int skillId) throws SQLException {
        List<score> list = new ArrayList<>();
        String sql = "SELECT id_score, id_user, id_skill, score, created_at " +
                "FROM score WHERE id_user=? AND id_skill=? ORDER BY created_at DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, skillId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            score s = new score();
            s.setIdScore(rs.getInt("id_score"));
            s.setIdUser(rs.getInt("id_user"));
            s.setIdSkill(rs.getInt("id_skill"));
            s.setScore(rs.getBigDecimal("score"));
            s.setCreatedAt(rs.getTimestamp("created_at"));
            list.add(s);
        }
        return list;
    }
}
