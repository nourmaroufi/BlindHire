package services;

import models.choiceqcm;
import utils.mydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class choiceqcmService {

    private final Connection cnx;

    public choiceqcmService() {
        cnx = mydb.getInstance().getConnection();
    }

    // CREATE: ajoute une réponse POUR une question (id_question obligatoire)
    public int addChoice(int idQuestion, String choiceText) throws SQLException {
        String sql = "INSERT INTO choice_qcm(id_question, choice_text, is_correct) VALUES (?,?,0)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, idQuestion);
        ps.setString(2, choiceText);
        ps.executeUpdate();

        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }


    public List<choiceqcm> getChoicesByQuestion(int idQuestion) throws SQLException {
        List<choiceqcm> list = new ArrayList<>();
        String sql = "SELECT id_choice, id_question, choice_text, is_correct " +
                "FROM choice_qcm WHERE id_question=? ORDER BY id_choice";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idQuestion);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            choiceqcm c = new choiceqcm();
            c.setIdChoice(rs.getInt("id_choice"));
            c.setIdQuestion(rs.getInt("id_question"));
            c.setChoiceText(rs.getString("choice_text"));
            c.setCorrect(rs.getBoolean("is_correct"));
            list.add(c);
        }
        return list;
    }





    public void updateChoiceText(int idChoice, String newText) throws SQLException {
        String sql = "UPDATE choice_qcm SET choice_text=? WHERE id_choice=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, newText);
        ps.setInt(2, idChoice);
        ps.executeUpdate();
    }

    public void deleteChoice(int idChoice) throws SQLException {
        String sql = "DELETE FROM choice_qcm WHERE id_choice=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idChoice);
        ps.executeUpdate();
    }

    // Helper Kahoot: 1 seule bonne réponse par question
    public void setCorrectChoice(int idQuestion, int idChoice) throws SQLException {
        cnx.setAutoCommit(false);
        try {
            PreparedStatement ps1 = cnx.prepareStatement(
                    "UPDATE choice_qcm SET is_correct=0 WHERE id_question=?");
            ps1.setInt(1, idQuestion);
            ps1.executeUpdate();

            PreparedStatement ps2 = cnx.prepareStatement(
                    "UPDATE choice_qcm SET is_correct=1 WHERE id_question=? AND id_choice=?");
            ps2.setInt(1, idQuestion);
            ps2.setInt(2, idChoice);
            ps2.executeUpdate();

            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }
}
