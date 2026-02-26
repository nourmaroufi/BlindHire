package DAO;

import Utils.MyDB;
import Model.Role;
import Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class userDAO {

    // ── Role parser ────────────────────────────────────────────────────────────
    private Role parseRole(String roleStr) {
        if (roleStr == null) return Role.client;
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(roleStr)) return r;
        }
        throw new IllegalArgumentException("Unknown role in DB: " + roleStr);
    }

    // ── ResultSet → User (reads all columns including nullable client fields) ──
    private User mapRow(ResultSet rs) throws Exception {
        return new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("mail"),
                rs.getString("mdp"),
                parseRole(rs.getString("role")),
                rs.getString("skills"),
                rs.getString("diplomas"),
                rs.getString("experience"),
                rs.getString("bio")
        );
    }

    // ── INSERT ────────────────────────────────────────────────────────────────
    public void insert(User user) {
        String sql = "INSERT INTO user(nom, prenom, mail, mdp, role, skills, diplomas, experience, bio) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMdp());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getSkills());       // null for recruteur/admin
            ps.setString(7, user.getDiplomas());
            ps.setString(8, user.getExperience());
            ps.setString(9, user.getBio());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inserting user: " + e.getMessage());
        }
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) users.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    // ── FIND BY EMAIL ─────────────────────────────────────────────────────────
    public User findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE mail = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void update(User user) {
        String sql = "UPDATE user SET nom=?, prenom=?, mail=?, mdp=?, role=?, " +
                "skills=?, diplomas=?, experience=?, bio=? WHERE id=?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  user.getNom());
            ps.setString(2,  user.getPrenom());
            ps.setString(3,  user.getEmail());
            ps.setString(4,  user.getMdp());
            ps.setString(5,  user.getRole().name());
            ps.setString(6,  user.getSkills());
            ps.setString(7,  user.getDiplomas());
            ps.setString(8,  user.getExperience());
            ps.setString(9,  user.getBio());
            ps.setInt(10,    user.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating user: " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void delete(int id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }
}