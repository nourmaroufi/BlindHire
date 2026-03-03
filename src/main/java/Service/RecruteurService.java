package Service;

import Model.Recruteur;
import utils.Mydb;

import java.sql.*;

public class RecruteurService {

    private Connection cnx;

    public RecruteurService() {
        cnx = Mydb.getInstance().getConnection();
    }

    public Integer login(String email, String password) throws SQLException {
        String sql = "SELECT id FROM recruteur WHERE email = ? AND password = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        }

        return null;
    }

}
