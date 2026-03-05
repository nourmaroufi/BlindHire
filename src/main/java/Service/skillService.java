package Service;

import Model.skill;
import Utils.Mydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class skillService {

    private final Connection cnx;

    public skillService() {
        cnx = Mydb.getInstance().getConnection();
    }

    // LIST: all skills
    public List<skill> getAllskills() throws SQLException {
        List<skill> list = new ArrayList<>();
        String sql = "SELECT id_skill, name, description FROM skill ORDER BY name ASC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            skill s = new skill();
            s.setIdSkill(rs.getInt("id_skill"));
            s.setName(rs.getString("name"));
            s.setDescription(rs.getString("description"));
            list.add(s);
        }
        return list;
    }

    // LIST: search by name (for admin search bar)
    public List<skill> searchskillsByName(String keyword) throws SQLException {
        List<skill> list = new ArrayList<>();
        String sql = "SELECT id_skill, name, description FROM skill WHERE name LIKE ? ORDER BY name ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            skill s = new skill();
            s.setIdSkill(rs.getInt("id_skill"));
            s.setName(rs.getString("name"));
            s.setDescription(rs.getString("description"));
            list.add(s);
        }
        return list;
    }

    // LIST: get one by id (optional but often needed)
    public skill getskillById(int idskill) throws SQLException {
        String sql = "SELECT id_skill, name, description FROM skill WHERE id_skill=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idskill);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            skill s = new skill();
            s.setIdSkill(rs.getInt("id_skill"));
            s.setName(rs.getString("name"));
            s.setDescription(rs.getString("description"));
            return s;
        }
        return null;
    }
}
