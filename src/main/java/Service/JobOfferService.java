package Service;
import Model.JobOffer;
import utils.Mydb;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobOfferService {
    private Connection cnx;

    public JobOfferService() {
        this.cnx = Mydb.getInstance().getConnection();
    }

    public void addJobOffer(JobOffer job) throws SQLException {
        String sql = "INSERT INTO job_offer (title, description, recruiter_id, type, status, posting_date) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, job.getTitle());
        ps.setString(2, job.getDescription());
        ps.setInt(3, job.getRecruiterId());
        ps.setString(4, job.getType());
        ps.setString(5, job.getStatus());
        if (job.getPostingDate() != null) {
            ps.setDate(6, Date.valueOf(job.getPostingDate()));
        } else {
            ps.setDate(6, null);
        }
        ps.executeUpdate();
    }

    public void updateJobOffer(JobOffer job) throws SQLException {
        String sql = "UPDATE job_offer SET title=?, description=?, type=?, status=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, job.getTitle());
        ps.setString(2, job.getDescription());
        ps.setString(3, job.getType());
        ps.setString(4, job.getStatus());
        ps.setInt(5, job.getId());

        System.out.println("Updating ID: " + job.getId());
        System.out.println("Type: " + job.getType());
        System.out.println("Status: " + job.getStatus());

        ps.executeUpdate();
    }


    public void deleteJobOffer(int id) throws SQLException {
        String sql = "DELETE FROM job_offer WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }


    public List<JobOffer> getJobOffers() throws SQLException {
        List<JobOffer> jobs = new ArrayList<>();
        String sql = "SELECT * FROM job_offer";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            JobOffer job = new JobOffer(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("recruiter_id"),
                    rs.getString("type"),
                    rs.getString("status"),
                    rs.getDate("posting_date") != null ? rs.getDate("posting_date").toLocalDate() : null
            );
            job.setId(rs.getInt("id"));
            jobs.add(job);
        }
        return jobs;
    }

    public int getJobCount() {
        String sql = "SELECT COUNT(*) AS total FROM job_offer";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public JobOffer getJobOfferById(int id) throws SQLException {
        String sql = "SELECT * FROM job_offer WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            JobOffer job = new JobOffer(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("recruiter_id"),
                    rs.getString("type"),
                    rs.getString("status"),
                    rs.getDate("posting_date") != null ? rs.getDate("posting_date").toLocalDate() : null
            );
            job.setId(rs.getInt("id"));
            return job;
        }
        return null;
    }

}