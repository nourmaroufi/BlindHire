package Service;

import Model.Candidat;
import Utils.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatService {

    private Connection cnx;

    public CandidatService() {
        this.cnx = Mydb.getInstance().getConnection();
    }

    public List<Candidat> afficherAll() throws SQLException {

        List<Candidat> list = new ArrayList<>();

        String query = "SELECT * FROM candidats";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Candidat c = new Candidat(
                    rs.getInt("id_candidat"),
                    rs.getInt("score"),
                    rs.getString("status")
            );
            list.add(c);
        }

        return list;
    }
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM candidats WHERE id_candidat = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
    }
    public void update(int id, int score, String status) throws SQLException {

        String sql = "UPDATE candidats SET score = ?, status = ? WHERE id_candidat = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, score);
        ps.setString(2, status);
        ps.setInt(3, id);

        ps.executeUpdate();
    }
}
