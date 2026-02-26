package Service;

import Model.Candidat;
import Model.Candidature;
import utils.Mydb;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandidatureService {
    private Connection cnx;

    public CandidatureService() {
        this.cnx = Mydb.getInstance().getConnection();
    }

    public List<Candidature> getCandidaturesByJobOfferId(int jobOfferId) throws SQLException {
        List<Candidature> list = new ArrayList<>();

        String query = "SELECT * FROM candidature WHERE job_id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, jobOfferId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Candidature(
                    rs.getInt("id"),
                    rs.getInt("candidate_id"),
                    rs.getInt("job_id"),
                    rs.getDate("application_date").toLocalDate(),
                    rs.getString("status")
            ));
        }

        return list;
    }

    public void addCandidature(Candidature candidature) throws SQLException {
        String sql = "INSERT INTO candidature (candidate_id, job_id, application_date, status, " +
                "cover_letter, cv_path, portfolio_url, expected_salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, 1);
        ps.setInt(2, candidature.getJobOfferId());
        ps.setDate(3, candidature.getApplicationDate() != null ?
                Date.valueOf(candidature.getApplicationDate()) : null);
        ps.setString(4, candidature.getStatus() != null ? candidature.getStatus() : "pending");
        ps.setString(5, candidature.getCoverLetter());
        ps.setString(6, candidature.getCvPath());
        ps.setString(7, candidature.getPortfolioUrl());
        ps.setDouble(8, candidature.getExpectedSalary() != null ? candidature.getExpectedSalary() : 0);

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            candidature.setId(rs.getInt(1));
        }
    }

    public void updateCandidature(Candidature candidature) throws SQLException {
        String sql = "UPDATE candidature SET status=?, cover_letter=?, cv_path=?, " +
                "portfolio_url=?, expected_salary=?, rejection_reason=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, candidature.getStatus());
        ps.setString(2, candidature.getCoverLetter());
        ps.setString(3, candidature.getCvPath());
        ps.setString(4, candidature.getPortfolioUrl());
        ps.setDouble(5, candidature.getExpectedSalary() != null ? candidature.getExpectedSalary() : 0);
        ps.setString(6, candidature.getRejectionReason());
        ps.setInt(7, candidature.getId());

        ps.executeUpdate();
    }

    public void deleteCandidature(int id) throws SQLException {
        String query = "DELETE FROM candidature WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Candidature> getAllCandidatures() throws SQLException {
        List<Candidature> candidatures = new ArrayList<>();
        String sql = "SELECT * FROM candidature ORDER BY application_date DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            candidatures.add(extractCandidatureFromResultSet(rs));
        }

        return candidatures;
    }


    private Candidature extractCandidatureFromResultSet(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        c.setCandidateId(rs.getInt("candidate_id"));
        c.setJobOfferId(rs.getInt("job_id"));

        Date appDate = rs.getDate("application_date");
        if (appDate != null) c.setApplicationDate(appDate.toLocalDate());

        c.setStatus(rs.getString("status"));

        // New fields
        c.setCoverLetter(rs.getString("cover_letter"));
        c.setCvPath(rs.getString("cv_path"));
        c.setPortfolioUrl(rs.getString("portfolio_url"));
        c.setExpectedSalary(rs.getDouble("expected_salary"));
        c.setRejectionReason(rs.getString("rejection_reason"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) c.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) c.setUpdatedAt(updatedAt.toLocalDateTime());

        return c;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM candidature WHERE status=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public List<Candidat> getAllCandidats() throws SQLException {
        List<Candidat> candidats = new ArrayList<>();
        String sql = "SELECT id, name, email FROM candidate";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Candidat c = new Candidat(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    "" // password not needed here
            );
            candidats.add(c);
        }
        return candidats;
    }    // Fetch a candidate by ID

    public Candidat getCandidatById(int id) throws SQLException {
        String sql = "SELECT * FROM candidate WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Candidat(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    ""
            );
        }
        return null;
    }

    public String getJobTitleById(int jobId) throws SQLException {
        String sql = "SELECT title FROM job_offer WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, jobId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getString("title");
        }
        return "Unknown Job";
    }
}
