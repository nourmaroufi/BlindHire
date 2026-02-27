package services;

import model.Interview;
import model.Candidat;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterviewService {

    Connection conn = MyConnection.getConnection();

    // ------------------- CREATE -------------------
    public void ajouter(Interview interview) throws SQLException {

        String sql = "INSERT INTO interview (id_candidat, date, type, job_offer, interviewer) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, interview.getId_candidat());
        ps.setTimestamp(2, Timestamp.valueOf(interview.getDate()));
        ps.setString(3, interview.getType());
        ps.setString(4, interview.getJob_offer());
        ps.setString(5, interview.getInterviewer());

        ps.executeUpdate();
        System.out.println("Interview added!");
    }

    // ------------------- READ -------------------
    public List<Interview> afficherAll() throws SQLException {

        List<Interview> list = new ArrayList<>();

        String sql = "SELECT * FROM interview";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Interview i = new Interview(
                    rs.getInt("id"),
                    rs.getInt("id_candidat"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("type"),
                    rs.getString("job_offer"),
                    rs.getString("interviewer")
            );

            list.add(i);
        }

        return list;
    }

    // ------------------- UPDATE -------------------
    public void update(int id, int candidateId, LocalDateTime dateTime, String type, String jobOffer, String interviewer) throws SQLException {

        String sql = "UPDATE interview SET id_candidat = ?, date = ?, type = ?, job_offer = ?, interviewer = ? WHERE id = ?";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, candidateId);
        ps.setTimestamp(2, Timestamp.valueOf(dateTime));
        ps.setString(3, type);
        ps.setString(4, jobOffer);
        ps.setString(5, interviewer);
        ps.setInt(6, id);

        ps.executeUpdate();
    }



    // ------------------- DELETE -------------------
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM interview WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Interview deleted!");
    }
    public List<Interview> afficherByCandidat(int candidatId) throws SQLException {

        List<Interview> list = new ArrayList<>();

        String query = "SELECT * FROM interview WHERE id_candidat = ? ORDER BY date ASC";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, candidatId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            Interview interview = new Interview(
                    rs.getInt("id"),
                    rs.getInt("id_candidat"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("type"),
                    rs.getString("job_offer"),
                    rs.getString("interviewer")
            );

            list.add(interview);
        }

        return list;
    }
}
